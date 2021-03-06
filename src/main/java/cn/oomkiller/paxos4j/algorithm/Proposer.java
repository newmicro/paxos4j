package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import cn.oomkiller.paxos4j.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class Proposer extends Base {
  private final Proposer.State proposerState;
  private final MsgCounter msgCounter;
  private final Learner learner;

  private boolean isPreparing;
  private boolean isAccepting;

  private final Timer timer = new Timer("ProposerTimer", true);
  private TimerTask prepareTimerTask;
  private long lastPrepareTimeoutMs;
  private TimerTask acceptTimerTask;
  private long lastAcceptTimeoutMs;
  private long timeoutInstanceId;

  boolean canSkipPrepare;
  boolean wasRejectBySomeone;

  public Proposer(Config config, MsgTransport msgTransport, Learner learner) {
    super(config, msgTransport);
    this.proposerState = new Proposer.State(config);
    this.msgCounter = new MsgCounter(config);
    this.learner = learner;

    isPreparing = false;
    isAccepting = false;
    canSkipPrepare = false;

    initForNewPaxosInstance();

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

  public void propose(byte[] value) {
    if (proposerState.getValue() == null) {
      proposerState.setValue(value);
    }

    lastPrepareTimeoutMs = config.getInitialPrepareTimeoutMs();
    lastAcceptTimeoutMs = config.getInitialAcceptTimeoutMs();

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
    addPrepareTimer();

    // 发送Prepare消息
    // send prepare message to all nodes except current node
    // this is an optimization to avoid conflict:
    // current node will not answer proposal from other nodes so no need to check current node's
    // preAccepted value
    msgTransport.broadcastMessageWithoutCurrentNode(paxosMsg);
  }

  public void onPrepareReply(PaxosMsg paxosMsg) {
    log.info("OnPrepareReply: " + paxosMsg);
    if (!shouldProcessMessage(paxosMsg, isPreparing)) {
      return;
    }

    if (paxosMsg.getRejectByPromiseId() == 0) {
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
      addPrepareTimer(RandomUtil.randomInt(30) + 10);
    }
  }

  public void onExpiredPrepareReply(PaxosMsg paxosMsg) {
    if (paxosMsg.getRejectByPromiseId() != 0) {
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }
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
    addAcceptTimer();
    msgTransport.broadcastMessageBeforeCurrentNode(paxosMsg);
  }

  public void onAcceptReply(PaxosMsg paxosMsg) {
    log.info("OnAcceptReply: " + paxosMsg);
    if (!shouldProcessMessage(paxosMsg, isAccepting)) {
      return;
    }

    if (paxosMsg.getRejectByPromiseId() == 0) {
      msgCounter.addPromiseOrAccept(paxosMsg.getNodeId());
    } else {
      msgCounter.addReject(paxosMsg.getNodeId());
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }

    if (msgCounter.isPassedOnThisRound()) {
      exitAccept();
      log.info("Value Accepted!!!");
      learner.proposerSendSuccess(getInstanceId(), proposerState.getProposalId());
    } else if (msgCounter.isRejectedOnThisRound() || msgCounter.isAllReceiveOnThisRound()) {
      addAcceptTimer(RandomUtil.randomInt(30) + 10);
    }
  }

  private boolean shouldProcessMessage(PaxosMsg paxosMsg, boolean inPhase) {
    if (!inPhase) {
      return false;
    }

    if (paxosMsg.getProposalId() != proposerState.getProposalId()) {
      return false;
    }

    msgCounter.addReceive(paxosMsg.getNodeId());
    return true;
  }

  public void onExpiredAcceptReply(PaxosMsg paxosMsg) {
    if (paxosMsg.getRejectByPromiseId() != 0) {
      wasRejectBySomeone = true;
      proposerState.setOtherProposalId(paxosMsg.getRejectByPromiseId());
    }
  }

  void onPrepareOrAcceptTimeout() {
    log.info("onPrepareOrAcceptTimeout");
    // 本轮提案已经选举结束，不再执行任何操作
    if (getInstanceId() != timeoutInstanceId) {
      return;
    }

    // 重新发起Prepare
    prepare(wasRejectBySomeone);
  }

  /////////////////////////////

  private void addPrepareTimer() {
    addPrepareTimer(0);
  }

  private void addPrepareTimer(final long timeoutMs) {
    if (prepareTimerTask != null) {
      prepareTimerTask.cancel();
    }

    prepareTimerTask =
        new TimerTask() {
          @Override
          public void run() {
            onPrepareOrAcceptTimeout();
          }
        };

    if (timeoutMs > 0) {
      timer.schedule(prepareTimerTask, timeoutMs);
      return;
    }

    timer.schedule(prepareTimerTask, lastPrepareTimeoutMs);
    timeoutInstanceId = getInstanceId();
    lastPrepareTimeoutMs *= 2;
    if (lastPrepareTimeoutMs > config.getMaxPrepareTimeoutMs()) {
      lastPrepareTimeoutMs = config.getMaxPrepareTimeoutMs();
    }
  }

  private void addAcceptTimer() {
    addAcceptTimer(0);
  }

  private void addAcceptTimer(long timeoutMs) {
    if (acceptTimerTask != null) {
      acceptTimerTask.cancel();
    }

    acceptTimerTask =
        new TimerTask() {
          @Override
          public void run() {
            onPrepareOrAcceptTimeout();
          }
        };

    if (timeoutMs > 0) {
      timer.schedule(acceptTimerTask, timeoutMs);
      return;
    }

    timer.schedule(acceptTimerTask, lastAcceptTimeoutMs);
    timeoutInstanceId = getInstanceId();
    lastAcceptTimeoutMs *= 2;
    if (lastAcceptTimeoutMs > config.getMaxAcceptTimeoutMs()) {
      lastAcceptTimeoutMs = config.getMaxAcceptTimeoutMs();
    }
  }

  private void exitPrepare() {
    if (isPreparing) {
      isPreparing = false;
      prepareTimerTask.cancel();
    }
  }

  private void exitAccept() {
    if (isAccepting) {
      isAccepting = false;
      acceptTimerTask.cancel();
    }
  }

  private void cancelSkipPrepare() {
    canSkipPrepare = false;
  }

  /////////////////////////////

  static class State {
    private long proposalId;
    private long highestOtherProposalId;
    private byte[] value;

    private BallotNumber highestOtherPreAcceptBallot = new BallotNumber();

    private Config config;

    public State(Config config) {
      this.config = config;
      proposalId = 1;
      init();
    }

    void init() {
      highestOtherProposalId = 0;
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
