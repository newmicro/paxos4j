package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.statemachine.StateMachine;
import cn.oomkiller.paxos4j.statemachine.StateMachineContext;
import cn.oomkiller.paxos4j.statemachine.StateMachineManager;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Instance {
  private static final long RETRY_QUEUE_MAX_LEN = 10;
  private Config config;
//  private MsgTransport msgTransport;
  private Proposer proposer;
  private Acceptor acceptor;
  private Learner learner;
  private StateMachineManager stateMachineManager;
//  private IOLoop ioLoop;
  private PaxosLog paxosLog;
  private boolean started;

  public Instance(Config config, MsgTransport msgTransport, LogStore logStore) {
    this.config = config;
//    this.msgTransport = msgTransport;
//    this.ioLoop = new IOLoop(config, this);
    this.acceptor = new Acceptor(config, msgTransport, logStore);
    this.learner = new Learner(config, msgTransport, acceptor, logStore, stateMachineManager);
    this.proposer = new Proposer(config, msgTransport, learner);
    this.paxosLog = new PaxosLog(logStore);
    this.started = false;
  }

  public void init() {
    // Must init acceptor first, because the max instanceid is record in acceptor state.
    acceptor.init();

    log.info("Acceptor.OK, Log.InstanceId " + acceptor.getInstanceId());

    long nowInstanceId = acceptor.getInstanceId();
    learner.setInstanceId(nowInstanceId);
    proposer.setInstanceId(nowInstanceId);
    proposer.setStartProposalId(acceptor.getAcceptorState().getPromiseBallot().getProposalId() + 1);

    learner.resetAskForLearnNoop();
  }

  public void start() {
    // start learner sender
    learner.startLearnerSender();
    // start ioloop
//    ioLoop.start();

    started = true;
  }

  public void stop() {
    if (started) {
//      ioLoop.stop();
      learner.stop();
    }
  }

  void newInstance() {
    acceptor.newInstance();
    learner.newInstance();
    proposer.newInstance();
  }

  byte[] getInstanceValue(long instanceId, byte[] value) {
    if (instanceId >= acceptor.getInstanceId()) {
      return null;
    }

    Acceptor.StateData state = null;
    try {
      state = paxosLog.readState(instanceId);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return state.getAcceptedValue();
  }

  public long getNowInstanceId() {
    return acceptor.getInstanceId();
  }

  public void onReceivePaxosMsg(final PaxosMsg paxosMsg, final boolean isRetry) {
    log.info("OnReceivePaxosMsg: " + paxosMsg);
    if (paxosMsg.getMsgType() == PaxosMsgType.PrepareReply
        || paxosMsg.getMsgType() == PaxosMsgType.AcceptReply
        || paxosMsg.getMsgType() == PaxosMsgType.SendNewValue) {
      if (!config.isValidNodeId(paxosMsg.getNodeId())) {
        log.error("acceptor reply type msg, from nodeid not in my membership, skip this message");
        return;
      }

      receiveMsgForProposer(paxosMsg);
    } else if (paxosMsg.getMsgType() == PaxosMsgType.Prepare
        || paxosMsg.getMsgType() == PaxosMsgType.Accept) {
      //      if ((!config.isValidNodeId(paxosMsg.getNodeId()))) {
      //        log.error(
      //            "prepare/accept type msg, from nodeid not in my membership(or i'm null
      // membership), skip this message and add node to tempnode, my gid %lu");
      //        config.addTmpNodeOnlyForLearn(paxosMsg.nodeid());
      //        return;
      //      }

      //            ChecksumLogic(paxosMsg);
      receiveMsgForAcceptor(paxosMsg, isRetry);
    } else if (paxosMsg.getMsgType() == PaxosMsgType.AskForLearn
        || paxosMsg.getMsgType() == PaxosMsgType.SendLearnValue
        || paxosMsg.getMsgType() == PaxosMsgType.ProposerSendSuccess
        || paxosMsg.getMsgType() == PaxosMsgType.ConfirmAskForLearn
        || paxosMsg.getMsgType() == PaxosMsgType.SendNowInstanceId
        || paxosMsg.getMsgType() == PaxosMsgType.SendLearnValueAck
        || paxosMsg.getMsgType() == PaxosMsgType.AskForCheckpoint) {
      //            ChecksumLogic(paxosMsg);
      receiveMsgForLearner(paxosMsg);
    } else {
      log.error("Invaid msgtype %d", paxosMsg.getMsgType());
    }
  }

  private void receiveMsgForAcceptor(PaxosMsg paxosMsg, boolean isRetry) {
    if (paxosMsg.getInstanceId() != acceptor.getInstanceId()) {
      //            BP->GetInstanceBP()->OnReceivePaxosAcceptorMsgInotsame();
    }

    if (paxosMsg.getInstanceId() == acceptor.getInstanceId() + 1) {
      // skip success message
      PaxosMsg newPaxosMsg = paxosMsg;
      newPaxosMsg.setInstanceId(acceptor.getInstanceId());
      newPaxosMsg.setMsgType(PaxosMsgType.ProposerSendSuccess);

      receiveMsgForLearner(newPaxosMsg);
    }

    if (paxosMsg.getInstanceId() == acceptor.getInstanceId()) {
      if (paxosMsg.getMsgType() == PaxosMsgType.Prepare) {
        acceptor.onPrepare(paxosMsg);
        return;
      } else if (paxosMsg.getMsgType() == PaxosMsgType.Accept) {
        acceptor.onAccept(paxosMsg);
      }
    } else if ((!isRetry) && (paxosMsg.getInstanceId() > acceptor.getInstanceId())) {
      // retry msg can't retry again.
      if (paxosMsg.getInstanceId() >= learner.getSeenLatestInstanceId()) {
        if (paxosMsg.getInstanceId() < acceptor.getInstanceId() + RETRY_QUEUE_MAX_LEN) {
          // need retry msg precondition
          // 1. prepare or accept msg
          // 2. msg.instanceid > nowinstanceid.
          //    (if < nowinstanceid, this msg is expire)
          // 3. msg.instanceid >= seen latestinstanceid.
          //    (if < seen latestinstanceid, proposer don't need reply with this instanceid
          // anymore.)
          // 4. msg.instanceid close to nowinstanceid.
          //                    ioLoop.addRetryPaxosMsg(paxosMsg);
        } else {
          // retry msg not series, no use.
//          ioLoop.clearRetryQueue();
        }
      }
    }
  }

  private void receiveMsgForProposer(PaxosMsg paxosMsg) {
    if (paxosMsg.getInstanceId() != proposer.getInstanceId()) {
      if (paxosMsg.getInstanceId() + 1 == proposer.getInstanceId()) {
        // Exipred reply msg on last instance.
        // If the response of a node is always slower than the majority node,
        // then the message of the node is always ignored even if it is a reject reply.
        // In this case, if we do not deal with these reject reply, the node that
        // gave reject reply will always give reject reply.
        // This causes the node to remain in catch-up state.
        //
        // To avoid this problem, we need to deal with the expired reply.
        if (paxosMsg.getMsgType() == PaxosMsgType.PrepareReply) {
          proposer.onExpiredPrepareReply(paxosMsg);
        } else if (paxosMsg.getMsgType() == PaxosMsgType.AcceptReply) {
          proposer.onExpiredAcceptReply(paxosMsg);
        }
      }
      return;
    }

    if (paxosMsg.getMsgType() == PaxosMsgType.PrepareReply) {
      proposer.onPrepareReply(paxosMsg);
    } else if (paxosMsg.getMsgType() == PaxosMsgType.AcceptReply) {
      proposer.onAcceptReply(paxosMsg);
    }
  }

  private void receiveMsgForLearner(PaxosMsg oPaxosMsg) {}

  public void commitNewValue(byte[] value) {
    proposer.propose(value);
  }

  /////////////////////
  public void addStateMachine(StateMachine stateMachine) {
    stateMachineManager.addStateMachine(stateMachine);
  }

  public void executeStateMachine(long instanceId, byte[] value, StateMachineContext ctx) {
    stateMachineManager.execute(instanceId, value, ctx);
  }
}
