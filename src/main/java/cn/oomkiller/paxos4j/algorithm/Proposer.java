package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Proposer extends Base {
  private Proposer.State proposerState;
  MsgCounter msgCounter;
  Learner learner;

  boolean isPreparing;
  boolean isAccepting;

  IOLoop ioLoop;

  int prepareTimerId;
  int lastPrepareTimeoutMs;
  int acceptTimerId;
  int lastAcceptTimeoutMs;
  long timeoutInstanceId;

  boolean canSkipPrepare;
  boolean wasRejectBySomeone;

  public Proposer(
      Config config, MsgTransport msgTransport, Instance instance, Learner learner, IOLoop ioLoop) {
    super(config, msgTransport);
    this.proposerState = new Proposer.State(config);
    this.msgCounter = new MsgCounter(config);
    this.learner = learner;
    this.ioLoop = ioLoop;

    isPreparing = false;
    isAccepting = false;
    canSkipPrepare = false;

    initForNewPaxosInstance();

    //        m_iPrepareTimerID = 0;
    //        m_iAcceptTimerID = 0;
    //        m_llTimeoutInstanceID = 0;

    lastPrepareTimeoutMs = config.getPrepareTimeoutMs();
    lastAcceptTimeoutMs = config.getAcceptTimeoutMs();

    wasRejectBySomeone = false;
  }

  void setStartProposalId(long proposalId) {
    proposerState.setStartProposalId(proposalId);
  }

  @Override
  public void initForNewPaxosInstance() {
    msgCounter.startNewRound();
    proposerState.init();

    exitPrepare();
    exitAccept();
  }

  boolean isWorking() {
    return isPreparing || isAccepting;
  }

  public void newValue(byte[] value) {
    if (proposerState.getValue() == null) {
      proposerState.setValue(value);
    }

    //        lastPrepareTimeoutMs = START_PREPARE_TIMEOUTMS;
    //        lastAcceptTimeoutMs = START_ACCEPT_TIMEOUTMS;

    // 如果允许跳过Prepare阶段 && 当前未被其他节点拒绝
    if (canSkipPrepare && !wasRejectBySomeone) {
      // 直接进入提案值确定阶段
      accept();
    } else {
      // 先进入Prepare阶段，随后进入提案值确定阶段
      // if not reject by someone, no need to increase ballot
      prepare(wasRejectBySomeone);
    }
  }

  // Prepare阶段
  public void prepare(boolean needNewBallot) {
    exitAccept();
    isPreparing = true;
    canSkipPrepare = false;
    wasRejectBySomeone = false;

    proposerState.resetHighestOtherPreAcceptBallot();

    // 分配新的提案编号
    if (needNewBallot) {
      proposerState.newPrepare();
    }

    PaxosMsg paxosMsg =
        PaxosMsg.builder()
            .msgType(PaxosMsgType.Prepare)
            .instanceId(getInstanceId())
            .nodeId(config.getMyNodeId())
            .proposalId(proposerState.getProposalId())
            .build();

    msgCounter.startNewRound();

    // Prepare超时定时器
    // ddPrepareTimer();

    // 发送Prepare消息
    msgTransport.broadcastMessage(paxosMsg);
  }

  public void onPrepareReply(PaxosMsg paxosMsg) {
    if (!isPreparing) {
      return;
    }

    if (paxosMsg.getProposalId() != proposerState.getProposalId()) {
      return;
    }

    // 已从node id节点获取数据
    msgCounter.addReceive(paxosMsg.getNodeId());

    if (paxosMsg.getRejectByPromiseId() == 0L) {
      // 接受该提案，并将该节点的promise id(承诺提案编号)、提案值更新到本地
      // 需要选取最大promise id对应的提案值
      BallotNumber ballot =
          new BallotNumber(paxosMsg.getPreAcceptId(), paxosMsg.getPreAcceptNodeId());
      msgCounter.addPromiseOrAccept(paxosMsg.getNodeId());
      proposerState.addPreAcceptValue(ballot, paxosMsg.getValue());
    } else {
      // 该提案被拒绝，标明本轮存在拒绝请求的节点(重新发起提案时需要更新提案编号)
      // 并将该节点已承诺的提案编号更新到本地
      msgCounter.addReject(paxosMsg.getNodeId());
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }

    // 已收到超过半数的Accept回复消息，直接进入Accept阶段
    if (msgCounter.isPassedOnThisRound()) {
      // 最近一次发起的Prepare被接受，后续可跳过Prepare阶段
      canSkipPrepare = true;
      // 进入Accept阶段
      accept();
    } else if (msgCounter.isRejectedOnThisRound() || msgCounter.isAllReceiveOnThisRound()) {
      // 已收到超过半数的Reject回复消息或者所有节点已回复(这个判断并不需要)，重新进入Prepare阶段
      log.info("[Not Pass] wait 30ms and restart prepare");
      // 重置Prepare超时定时器，提前触发Prepare超时
      //            addPrepareTimer(OtherUtils::FastRand() % 30 + 10);
    }
  }

  public void onExpiredPrepareReply(PaxosMsg paxosMsg) {
    if (paxosMsg.getRejectByPromiseId() != 0L) {
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }
  }

  void onPrepareTimeout() {
    // 本轮提案已经选举结束，不再执行任何操作
    if (getInstanceId() != timeoutInstanceId) {
      return;
    }

    // 重新发起Prepare
    prepare(wasRejectBySomeone);
  }

  public void accept() {
    exitPrepare();
    isAccepting = true;

    PaxosMsg paxosMsg =
        PaxosMsg.builder()
            .msgType(PaxosMsgType.Accept)
            .instanceId(getInstanceId())
            .nodeId(config.getMyNodeId())
            .proposalId(proposerState.getProposalId())
            .value(proposerState.getValue())
            .build();

    msgCounter.startNewRound();
    //        AddAcceptTimer();
    msgTransport.broadcastMessage(paxosMsg);
//    instance.onReceivePaxosMsg(paxosMsg, false);
  }

  public void onAcceptReply(PaxosMsg paxosMsg) {
    if (!isAccepting) {
      return;
    }

    if (paxosMsg.getProposalId() != proposerState.getProposalId()) {
      return;
    }

    msgCounter.addReceive(paxosMsg.getNodeId());

    if (paxosMsg.getRejectByPromiseId() == 0L) {
      msgCounter.addPromiseOrAccept(paxosMsg.getNodeId());
    } else {
      msgCounter.addReject(paxosMsg.getNodeId());
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }

    if (msgCounter.isPassedOnThisRound()) {
      exitAccept();
      learner.proposerSendSuccess(getInstanceId(), proposerState.getProposalId());
    } else if (msgCounter.isRejectedOnThisRound() || msgCounter.isAllReceiveOnThisRound()) {
      //            AddAcceptTimer(OtherUtils::FastRand() % 30 + 10);
    }
  }

  public void onExpiredAcceptReply(PaxosMsg paxosMsg) {
    if (paxosMsg.getRejectByPromiseId() != 0L) {
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }
  }

  public void onAcceptTimeout() {
    // 本轮提案已经选举结束，不再执行任何操作
    if (getInstanceId() != timeoutInstanceId) {
      return;
    }

    // 重新发起Prepare
    prepare(wasRejectBySomeone);
  }

  private void exitPrepare() {
    if (isPreparing) {
      isPreparing = false;
      ioLoop.removeTimer(prepareTimerId);
    }
  }

  private void exitAccept() {
    if (isAccepting) {
      isAccepting = false;
      ioLoop.removeTimer(acceptTimerId);
    }
  }

  private void cancelSkipPrepare() {
    canSkipPrepare = false;
  }

  /////////////////////////////

  void addPrepareTimer(int timeoutMs) {}

  void addAcceptTimer(int timeoutMs) {}

  static class State {
    private long proposalId;
    private long highestOtherProposalId;
    private byte[] value;

    private BallotNumber highestOtherPreAcceptBallot;

    private Config config;

    public State(Config config) {
      this.config = config;
      proposalId = 1L;
      init();
    }

    void init() {
      highestOtherProposalId = 0L;
      value = null;
    }

    void setStartProposalId(long proposalId) {
      this.proposalId = proposalId;
    }

    void newPrepare() {
      long maxProposalId =
          proposalId > highestOtherProposalId ? proposalId : highestOtherProposalId;
      proposalId = maxProposalId + 1;
    }

    void addPreAcceptValue(BallotNumber otherPreAcceptBallot, byte[] otherPreAcceptValue) {
      if (otherPreAcceptBallot.isNull()) {
        return;
      }

      if (otherPreAcceptBallot.greaterThan(highestOtherPreAcceptBallot)) {
        highestOtherPreAcceptBallot = otherPreAcceptBallot;
        value = otherPreAcceptValue;
      }
    }

    /////////////////////////

    long getProposalId() {
      return proposalId;
    }

    byte[] getValue() {
      return value;
    }

    void setValue(byte[] value) {
      this.value = value;
    }

    void setOtherProposalId(long otherProposalId) {
      if (otherProposalId > highestOtherProposalId) {
        highestOtherProposalId = otherProposalId;
      }
    }

    void resetHighestOtherPreAcceptBallot() {
      highestOtherPreAcceptBallot.reset();
    }
  }
}
