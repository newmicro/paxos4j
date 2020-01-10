package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.statemachine.StateMachineManager;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Learner extends Base {
  private final Learner.State learnerState;
  private final PaxosLog paxosLog;
  private final Acceptor acceptor;
  private final StateMachineManager stateMachineManager;

  private long highestSeenInstanceId;
  private long highestSeenInstanceIdFromNodeId;
  private boolean learning;
  private long m_llLastAckInstanceID;

  private LearnerSender learnerSender;

  private Timer timer = new Timer("LearnerTimer", true);
  private TimerTask askForLearnNoopTimerTask;

  public Learner(
      Config config,
      MsgTransport msgTransport,
      Acceptor acceptor,
      LogStore logStore,
      StateMachineManager stateMachineManager) {
    super(config, msgTransport);
    this.learnerState = new Learner.State(config, logStore);
    this.paxosLog = new PaxosLog(logStore);
    this.acceptor = acceptor;
    this.stateMachineManager = stateMachineManager;

    initForNewPaxosInstance();

    highestSeenInstanceId = 0;
    highestSeenInstanceIdFromNodeId = 0;

    learning = false;

    m_llLastAckInstanceID = 0;
  }

  @Override
  public void initForNewPaxosInstance() {
    learnerState.init();
  }

  public boolean isLearned() {
    return learnerState.isLearned();
  }

  public byte[] getLearnedValue() {
    return learnerState.getLearnedValue();
  }

  public boolean isLatest() {
    return (getInstanceId() + 1) >= highestSeenInstanceId;
  }

  public long getSeenLatestInstanceId() {
    return highestSeenInstanceId;
  }

  public void setSeenInstanceId(long llInstanceID, long llFromNodeID) {
    if (llInstanceID > highestSeenInstanceId) {
      highestSeenInstanceId = llInstanceID;
      highestSeenInstanceIdFromNodeId = llFromNodeID;
    }
  }

  public void askForLearnNoop() {
    askForLearnNoop(false);
  }

  public void askForLearnNoop(boolean isStart) {
    resetAskForLearnNoop();

    learning = false;

    //    m_poCheckpointMgr->ExitCheckpointMode();

    askForLearn();

    if (isStart) {
      askForLearn();
    }
  }

  private void askForLearn() {
    PaxosMsg oPaxosMsg =
        PaxosMsg.builder()
            .instanceId(getInstanceId())
            .nodeId(config.getMyNodeId())
            .msgType(PaxosMsgType.AskForLearn)
            .build();

    if (config.isFollower()) {
      // this is not proposal nodeid, just use this val to bring followto nodeid info.
      oPaxosMsg.setProposalNodeId(config.getFollowToNodeId());
    }

    log.info("END InstanceID %lu MyNodeID %lu", oPaxosMsg.getInstanceId(), oPaxosMsg.getNodeId());

    msgTransport.broadcastMessageWithoutCurrentNode(oPaxosMsg);
    //    msgTransport.broadcastMessageToTempNode(oPaxosMsg, Message_SendType_UDP);
  }

  public void onAskforLearn(final PaxosMsg paxosMsg) {
    log.info(
        "START Msg.InstanceID %lu Now.InstanceID %lu Msg.from_nodeid %lu MinChosenInstanceID %lu",
        paxosMsg.getInstanceId(), getInstanceId(), paxosMsg.getNodeId());

    setSeenInstanceId(paxosMsg.getInstanceId(), paxosMsg.getNodeId());

    if (paxosMsg.getProposalNodeId() == config.getMyNodeId()) {
      // Found a node follow me.
      log.info("Found a node %lu follow me.", paxosMsg.getNodeId());
      config.addFollowerNode(paxosMsg.getNodeId());
    }

    if (paxosMsg.getInstanceId() >= getInstanceId()) {
      return;
    }

    if (paxosMsg.getInstanceId() >= 0) // m_poCheckpointMgr->GetMinChosenInstanceID())
    {
      if (!learnerSender.prepare(paxosMsg.getInstanceId(), paxosMsg.getNodeId())) {
        log.error("LearnerSender working for others.");

        if (paxosMsg.getInstanceId() == (getInstanceId() - 1)) {
          log.info("InstanceID only difference one, just send this value to other.");
          // send one value
          Acceptor.StateData state = null;
          try {
            state = paxosLog.readState(paxosMsg.getInstanceId());
          } catch (IOException e) {
            e.printStackTrace();
          }
          BallotNumber ballot = new BallotNumber(state.getPromiseId(), state.getPromiseNodeId());
          //          SendLearnValue(paxosMsg.nodeid(), paxosMsg.instanceid(), oBallot,
          // oState.acceptedvalue(), 0, false);
        }

        return;
      }
    }

    sendNowInstanceId(paxosMsg.getInstanceId(), paxosMsg.getNodeId());
  }

  private void sendNowInstanceId(long instanceId, long sendNodeId) {
    PaxosMsg paxosMsg =
        PaxosMsg.builder()
            .instanceId(instanceId)
            .nodeId(config.getMyNodeId())
            .msgType(PaxosMsgType.SendNowInstanceId)
            .nowInstanceId(getInstanceId())
            .build();

    //    if ((getInstanceId() - instanceId) > 50)
    //    {
    //      //instanceid too close not need to send vsm/master checkpoint.
    //      string sSystemVariablesCPBuffer;
    //      int ret = m_poConfig->GetSystemVSM()->GetCheckpointBuffer(sSystemVariablesCPBuffer);
    //      if (ret == 0)
    //      {
    //        paxosMsg.set_systemvariables(sSystemVariablesCPBuffer);
    //      }
    //
    //      string sMasterVariablesCPBuffer;
    //      if (m_poConfig->GetMasterSM() != nullptr)
    //      {
    //        int ret = m_poConfig->GetMasterSM()->GetCheckpointBuffer(sMasterVariablesCPBuffer);
    //        if (ret == 0)
    //        {
    //          paxosMsg.set_mastervariables(sMasterVariablesCPBuffer);
    //        }
    //      }
    //    }
    //
    //    SendMessage(sendNodeId, paxosMsg);
  }

  public void resetAskForLearnNoop() {
    resetAskForLearnNoop(config.getAskforLearnInterval());
  }

  public void resetAskForLearnNoop(long timeoutMs) {
    if (askForLearnNoopTimerTask != null) {
      askForLearnNoopTimerTask.cancel();
    }

    askForLearnNoopTimerTask =
        new TimerTask() {
          @Override
          public void run() {
            // onTimeour();
          }
        };

    timer.schedule(askForLearnNoopTimerTask, timeoutMs);
  }

  public void proposerSendSuccess(long instanceId, long proposalId) {
    PaxosMsg paxosMsg =
        PaxosMsg.builder()
            .msgType(PaxosMsgType.ProposerSendSuccess)
            .instanceId(instanceId)
            .nodeId(config.getMyNodeId())
            .proposalId(proposalId)
            .build();

    // run self first
    msgTransport.broadcastMessageAfterCurrentNode(paxosMsg);
  }

  public void startLearnerSender() {}

  public void stop() {
    learnerSender.stop();
  }

  //////////////////////////////////////

  @Data
  static class State {
    private byte[] learnedValue;
    private boolean learned;
    private Config config;
    private PaxosLog paxosLog;

    State(Config config, LogStore logStore) {
      this.config = config;
      this.paxosLog = new PaxosLog(logStore);
      init();
    }

    void init() {
      learned = false;
      learnedValue = null;
    }

    void learnValue(long instanceId, BallotNumber learnedBallot, byte[] value) {
      Acceptor.StateData stateData =
          Acceptor.StateData.builder()
              .instanceId(instanceId)
              .acceptedValue(value)
              .promiseId(learnedBallot.getProposalId())
              .promiseNodeId(learnedBallot.getNodeId())
              .acceptedId(learnedBallot.getProposalId())
              .acceptedNodeId(learnedBallot.getNodeId())
              .build();

      try {
        paxosLog.writeState(instanceId, stateData, true);
      } catch (IOException e) {
        e.printStackTrace();
      }

      learnValueWithoutWrite(instanceId, value);

      log.info("OK, InstanceID %s ValueLen %s checksum %u", instanceId, value.length);
    }

    void learnValueWithoutWrite(long instanceId, byte[] value) {
      learnedValue = value;
      learned = true;
    }
  }
}
