package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStorage;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Acceptor extends Base {
  private Acceptor.State acceptorState;

  public Acceptor(Config config, MsgTransport msgTransport, LogStorage logStorage) {
    super(config, msgTransport);
    this.acceptorState = new Acceptor.State(config, logStorage);
  }

  void init() {
    long instanceId = acceptorState.load();
    if (instanceId == 0L) {
      log.info("Empty database");
    }

    setInstanceId(instanceId);
    log.info("OK");
  }

  public void initForNewPaxosInstance() {
    acceptorState.init();
  }

  public Acceptor.State getAcceptorState() {
    return this.acceptorState;
  }

  void onPrepare(PaxosMsg paxosMsg) {
    PaxosMsg replyMsg =
        PaxosMsg.builder()
            .msgType(PaxosMsgType.PrepareReply)
            .instanceId(getInstanceId())
            .nodeId(config.getMyNodeId())
            .proposalId(paxosMsg.getProposalId())
            .build();

    BallotNumber ballot = new BallotNumber(paxosMsg.getProposalId(), paxosMsg.getNodeId());

    //
    if (ballot.greaterThanOrEqual(acceptorState.getPromiseBallot())) {
      replyMsg.setPreAcceptId(acceptorState.getAcceptedBallot().getProposalId());
      replyMsg.setPreAcceptNodeId(acceptorState.getAcceptedBallot().getNodeId());

      if (acceptorState.getAcceptedBallot().notNull()) {
        replyMsg.setValue(acceptorState.getAcceptedValue());
      }
      acceptorState.setPromiseBallot(ballot);

      acceptorState.persist(getInstanceId());
    } else {
      replyMsg.setRejectByPromiseId(acceptorState.getPromiseBallot().getProposalId());
    }

    long replyNodeId = paxosMsg.getNodeId();
    msgTransport.sendMessage(replyNodeId, replyMsg);
  }

  void onAccept(PaxosMsg paxosMsg) {
    log.info("OnAccept");
    PaxosMsg replyMsg =
        PaxosMsg.builder()
            .msgType(PaxosMsgType.AcceptReply)
            .instanceId(getInstanceId())
            .nodeId(config.getMyNodeId())
            .proposalId(paxosMsg.getProposalId())
            .build();

    BallotNumber ballot = new BallotNumber(paxosMsg.getProposalId(), paxosMsg.getNodeId());

    if (ballot.greaterThanOrEqual(acceptorState.getPromiseBallot())) {
      acceptorState.setPromiseBallot(ballot);
      acceptorState.setAcceptedBallot(ballot);
      acceptorState.setAcceptedValue(paxosMsg.getValue());

      acceptorState.persist(getInstanceId());
    } else {
      replyMsg.setRejectByPromiseId(acceptorState.getPromiseBallot().getProposalId());
    }

    long replyNodeId = paxosMsg.getNodeId();
    msgTransport.sendMessage(replyNodeId, replyMsg);
  }

  @Getter
  @Setter
  public static class State {
    private BallotNumber promiseBallot = new BallotNumber();
    private BallotNumber acceptedBallot = new BallotNumber();
    private byte[] acceptedValue;

    private Config config;
    private PaxosLog paxosLog;

    public State(Config config, LogStorage logStorage) {
      this.config = config;
      this.paxosLog = new PaxosLog(logStorage);
      init();
    }

    public void persist(long instanceId) {
      paxosLog.writeState(instanceId, this);
    }

    public long load() {
      long instanceId = paxosLog.getMaxInstanceIdFromLog();
      if (instanceId != 0L) {
        Acceptor.State acceptorState = paxosLog.readState(instanceId);
        promiseBallot = acceptorState.getPromiseBallot();
        acceptedBallot = acceptorState.getAcceptedBallot();
        acceptedValue = acceptorState.getAcceptedValue();
      }

      return instanceId;
    }

    public void init() {
      acceptedBallot.reset();
      acceptedValue = null;
    }
  }
}
