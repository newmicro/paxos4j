package cn.oomkiller.paxos4j.log;

import cn.oomkiller.paxos4j.algorithm.Acceptor;
import cn.oomkiller.paxos4j.message.AcceptorStateData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PaxosLog {
  private LogStorage logStorage;

  public void writeLog(long instanceId, byte[] value) {
    AcceptorStateData stateData =
        AcceptorStateData.builder()
            .instanceId(instanceId)
            .acceptedValue(value)
            .promiseId(0)
            .promiseNodeId(0L)
            .acceptedId(0)
            .acceptedNodeId(0L)
            .build();

    writeState(instanceId, stateData);
  }

  public byte[] readLog(long instanceId) {
    return null;
  }

  public long getMaxInstanceIdFromLog() {
    return 0L;
  }

  public void writeState(long instanceId, AcceptorStateData stateData) {}

  public void writeState(long instanceId, Acceptor.State state) {}

  public Acceptor.State readState(long instanceId) {
    return null;
  }
}
