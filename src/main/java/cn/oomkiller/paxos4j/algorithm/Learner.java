package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStorage;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.message.AcceptorStateData;
import cn.oomkiller.paxos4j.statemachine.StateMachineManager;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Learner extends Base {
  private final Learner.State learnerState;
  private final PaxosLog paxosLog;
  private final Acceptor acceptor;
  private final StateMachineManager stateMachineManager;

  private long m_llHighestSeenInstanceID;
  private long m_iHighestSeenInstanceID_FromNodeID;
  private boolean amILearning;
  private long m_llLastAckInstanceID;

  private LearnerSender learnerSender;

  public Learner(
      Config config,
      MsgTransport msgTransport,
      Acceptor acceptor,
      LogStorage logStorage,
      StateMachineManager stateMachineManager) {
    super(config, msgTransport);
    this.learnerState = new Learner.State(config, logStorage);
    this.paxosLog = new PaxosLog(logStorage);
    this.acceptor = acceptor;
    this.stateMachineManager = stateMachineManager;

    initForNewPaxosInstance();

    m_llHighestSeenInstanceID = 0;
    m_iHighestSeenInstanceID_FromNodeID = 0;

    amILearning = false;

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

  public boolean amILatest() {
    return (getInstanceId() + 1) >= m_llHighestSeenInstanceID;
  }

  public long getSeenLatestInstanceId() {
    return m_llHighestSeenInstanceID;
  }

  public void setSeenInstanceId(long llInstanceID, long llFromNodeID)
  {
    if (llInstanceID > m_llHighestSeenInstanceID)
    {
      m_llHighestSeenInstanceID = llInstanceID;
      m_iHighestSeenInstanceID_FromNodeID = llFromNodeID;
    }
  }

  public void askForLearnNoop() {
    askForLearnNoop(false);
  }

  public void askForLearnNoop(boolean isStart) {

  }

  public void resetAskForLearnNoop() {
    resetAskForLearnNoop(config.getAskforLearnInterval());
  }

  public void resetAskForLearnNoop(long timeoutMs) {

  }

  public void proposerSendSuccess(long instanceId, long proposalId) {}

  public void startLearnerSender() {

  }

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

    State(Config config, LogStorage logStorage) {
      this.config = config;
      this.paxosLog = new PaxosLog(logStorage);
      init();
    }

    void init() {
      learned = false;
      learnedValue = null;
    }

    void learnValue(long instanceId, BallotNumber learnedBallot, byte[] value) {
      AcceptorStateData stateData =
          AcceptorStateData.builder()
              .instanceId(instanceId)
              .acceptedValue(value)
              .promiseId(learnedBallot.getProposalId())
              .promiseNodeId(learnedBallot.getNodeId())
              .acceptedId(learnedBallot.getProposalId())
              .acceptedNodeId(learnedBallot.getNodeId())
              .build();

      paxosLog.writeState(instanceId, stateData);

      learnValueWithoutWrite(instanceId, value);

      log.info("OK, InstanceID %s ValueLen %s checksum %u", instanceId, value.length);
    }

    void learnValueWithoutWrite(long instanceId, byte[] value) {
      learnedValue = value;
      learned = true;
    }
  }
}
