package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.log.LogStorage;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class Config {
  private boolean logSync;
  private int syncInterval;
  private boolean useMembership;
  private boolean largeValueMode;

  private long myNodeId;
  private int nodeCount;
  private int myGroupIdx;
  private int groupCount;

  private List<NodeInfo> nodeInfoList;

  private boolean amIFollower;
  private long followToNodeId;

  private Map<Long, Long> mapTmpNodeOnlyForLearn;
  private Map<Long, Long> mapMyFollower;

  public Config(
      LogStorage logStorage,
      boolean logSync,
      int syncInterval,
      boolean useMembership,
      boolean largeValueMode,
      NodeInfo myNode,
      List<NodeInfo> nodeInfoList) {
    this.logSync = logSync;
    this.syncInterval = syncInterval;
    this.useMembership = useMembership;
    this.myNodeId = myNode.getNodeId();
    this.nodeInfoList = nodeInfoList;
    this.largeValueMode = largeValueMode;
  }

  public void init() {}

  public void checkConfig() {}

  public boolean isValidNodeId(long nodeId) {
    return true;
  }

  public long getInitialPrepareTimeoutMs() {
    if (largeValueMode) {
      return 15000L;
    } else {
      return 2000L;
    }
  }

  public long getInitialAcceptTimeoutMs() {
    if (largeValueMode) {
      return 15000L;
    } else {
      return 1000L;
    }
  }

  public long getMaxPrepareTimeoutMs() {
    if (largeValueMode) {
      return 90000L;
    } else {
      return 8000L;
    }
  }

  public long getMaxAcceptTimeoutMs() {
    if (largeValueMode) {
      return 90000L;
    } else {
      return 8000L;
    }
  }

  public int getMajorityCount() {
    return nodeInfoList.size() / 2 + 1;
  }
}
