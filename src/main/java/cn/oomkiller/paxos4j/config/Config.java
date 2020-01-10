package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.utils.RandomUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

  private boolean follower;
  private long followToNodeId;

  private Map<Long, Long> mapTmpNodeOnlyForLearn;
  private Map<Long, Long> mapMyFollower;

  public Config(
      LogStore logStore,
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

  public long getPrepareTimeoutMs() {
    return 3000L;
  }

  public long getAcceptTimeoutMs() {
    return 3000L;
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

  public long getAskforLearnInterval() {
    if (!follower) {
      if (largeValueMode) {
        return 50000 + (RandomUtil.randomInt() % 10000);
      } else {
        return 2500 + (RandomUtil.randomInt() % 500);
      }
    } else {
      if (largeValueMode) {
        return 30000 + (RandomUtil.randomInt() % 15000);
      } else {
        return 2000 + (RandomUtil.randomInt() % 1000);
      }
    }
  }

  public int getMajorityCount() {
    return nodeInfoList.size() / 2 + 1;
  }

  public void addFollowerNode(long nodeId) {

  }
}
