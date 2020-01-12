package cn.oomkiller.paxos4j.log;

import cn.oomkiller.paxos4j.algorithm.Acceptor;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class PaxosLog {
  private final LogStore logStore;

  public void writeLog(long instanceId, byte[] value, boolean isSync) throws IOException {
    Acceptor.StateData stateData =
        Acceptor.StateData.builder()
            .instanceId(instanceId)
            .acceptedValue(value)
            .promiseId(0)
            .promiseNodeId(0)
            .acceptedId(0)
            .acceptedNodeId(0)
            .build();

    writeState(instanceId, stateData, isSync);
  }

  public byte[] readLog(long instanceId) throws IOException {
    Acceptor.StateData state = readState(instanceId);
    return state.getAcceptedValue();
  }

  public long getMaxInstanceIdFromLog() {
    return 0L;
  }

  public void writeState(long instanceId, Acceptor.StateData state, boolean isSync)
      throws IOException {
    byte[] bytes = state.serializeToBytes();
    logStore.put(instanceId, bytes, isSync);
  }

  public Acceptor.StateData readState(long instanceId) throws IOException {
    byte[] bytes = logStore.get(instanceId);
    Acceptor.StateData stateData = new Acceptor.StateData();

    return stateData.parseFromBytes(bytes);
  }
}
