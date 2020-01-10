package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.message.PaxosMsgType;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Acceptor extends Base {
  private final Acceptor.State acceptorState;

  public Acceptor(Config config, MsgTransport msgTransport, LogStore logStore) {
    super(config, msgTransport);
    this.acceptorState = new Acceptor.State(config, logStore);
  }

  void init() {
    long instanceId = acceptorState.load();
    if (instanceId == 0) {
      log.info("Empty database");
    }

    setInstanceId(instanceId);
    log.info("OK");
  }

  public void initForNewPaxosInstance() {
    acceptorState.init();
  }

  Acceptor.State getAcceptorState() {
    return this.acceptorState;
  }

  void onPrepare(PaxosMsg paxosMsg) {
    log.info("OnPrepare: " + paxosMsg);
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

      try {
        acceptorState.persist(getInstanceId());
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      replyMsg.setRejectByPromiseId(acceptorState.getPromiseBallot().getProposalId());
    }

    long replyNodeId = paxosMsg.getNodeId();
    msgTransport.sendMessage(replyNodeId, replyMsg);
  }

  void onAccept(PaxosMsg paxosMsg) {
    log.info("OnAccept " + paxosMsg);
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

      try {
        acceptorState.persist(getInstanceId());
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      replyMsg.setRejectByPromiseId(acceptorState.getPromiseBallot().getProposalId());
    }

    long replyNodeId = paxosMsg.getNodeId();
    msgTransport.sendMessage(replyNodeId, replyMsg);
  }

  @Getter
  @Setter
  static class State {
    private final Config config;
    private final PaxosLog paxosLog;

    private BallotNumber promiseBallot = new BallotNumber();
    private BallotNumber acceptedBallot = new BallotNumber();
    private byte[] acceptedValue;

    private int syncTimes = 0;

    public State(Config config, LogStore logStore) {
      this.config = config;
      this.paxosLog = new PaxosLog(logStore);
      init();
    }

    public void init() {
      acceptedBallot.reset();
      acceptedValue = null;
    }

    public long load() {
      long instanceId = paxosLog.getMaxInstanceIdFromLog();
      if (instanceId != 0) {
        StateData state = null;
        try {
          state = paxosLog.readState(instanceId);
        } catch (IOException e) {
          e.printStackTrace();
        }
        promiseBallot.setProposalId(state.promiseId);
        promiseBallot.setNodeId(state.promiseNodeId);
        acceptedBallot.setProposalId(state.acceptedId);
        acceptedBallot.setNodeId(state.acceptedNodeId);
        acceptedValue = state.acceptedValue;
      }

      return instanceId;
    }

    public void persist(long instanceId) throws IOException {
      Acceptor.StateData state =
          StateData.builder()
              .instanceId(instanceId)
              .promiseId(promiseBallot.getProposalId())
              .promiseNodeId(promiseBallot.getNodeId())
              .acceptedId(acceptedBallot.getProposalId())
              .acceptedNodeId(acceptedBallot.getNodeId())
              .acceptedValue(acceptedValue)
              .build();

      boolean isSync = config.isLogSync();
      if (isSync) {
        syncTimes++;
        if (syncTimes > config.getSyncInterval()) {
          syncTimes = 0;
        } else {
          isSync = false;
        }
      }
      paxosLog.writeState(instanceId, state, isSync);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  public static class StateData {
    long instanceId;
    long promiseId;
    long promiseNodeId;
    long acceptedId;
    long acceptedNodeId;
    byte[] acceptedValue;

    private int size() {
      if (acceptedValue != null) {
        return acceptedValue.length + 20;
      }
      return 0;
    }

    public StateData parseFromBytes(byte[] bytes) throws IOException {
      return parseFromBytes(bytes, 0, bytes.length);
    }

    public StateData parseFromBytes(byte[] bytes, int offset, int length) throws IOException {
//      return JsonUtil.fromJson(new String(bytes, offset, length), StateData.class);
      try (ByteArrayInputStream buffer = new ByteArrayInputStream(bytes, offset, length);
          DataInputStream in = new DataInputStream(buffer)) {
        instanceId = in.readLong();
        promiseId = in.readLong();
        promiseNodeId = in.readLong();
        acceptedId = in.readLong();
        acceptedNodeId = in.readLong();
        acceptedValue = new byte[length - 20];
        in.readFully(acceptedValue);
        return this;
      }
    }

    public byte[] serializeToBytes() throws IOException {
      try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(size());
          DataOutputStream out = new DataOutputStream(buffer)) {
        out.writeLong(instanceId);
        out.writeLong(promiseId);
        out.writeLong(promiseNodeId);
        out.writeLong(acceptedId);
        out.writeLong(acceptedNodeId);
        out.write(acceptedValue);
        return buffer.toByteArray();
      }
    }
  }
}
