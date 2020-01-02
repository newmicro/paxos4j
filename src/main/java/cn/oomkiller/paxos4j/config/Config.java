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
      NodeInfo myNode,
      List<NodeInfo> nodeInfoList) {}

  public void init() {}

  public void checkConfig() {}
}
