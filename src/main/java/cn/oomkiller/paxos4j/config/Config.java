package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.log.LogStorage;

import java.util.List;
import java.util.Map;

public class Config {
    private boolean logSync;
    private int syncInterval;
    private boolean useMembership;

    private long myNodeId;
    private int nodeCount;
    private int myGroupIdx;
    private int groupCount;

    private List<NodeInfo> nodeInfoList;

    private boolean isIMFollower;
    private long followToNodeId;

    private Map<Long, Long> mapTmpNodeOnlyForLearn;
    private Map<Long, Long> mapMyFollower;

    public Config(
        LogStorage logStorage,
        boolean logSync,
        int syncInterval,
        boolean useMembership,
        NodeInfo myNode,
        List<NodeInfo> nodeInfoList) {

    }

    int init();

    boolean checkConfig();

    public long GetGid();

    long GetMyNodeID();

    int GetNodeCount();

    int GetMyGroupIdx();

    int GetGroupCount();

    int GetMajorityCount();

    boolean GetIsUseMembership();

    public int GetPrepareTimeoutMs();

    int GetAcceptTimeoutMs();

    long GetAskforLearnTimeoutMs();

    public:
            boolean IsValidNodeID(long iNodeID);

    boolean IsIMFollower();

    long GetFollowToNodeID();

    boolean LogSync();

    int SyncInterval();

    void SetLogSync(boolean bLogSync);

    public void SetMasterSM(InsideSM poMasterSM);

    InsideSM GetMasterSM();

    public:
    void AddTmpNodeOnlyForLearn(long iTmpNodeID);

    //this function only for communicate.
    std::map<long, long> GetTmpNodeMap();

    void AddFollowerNode(long iMyFollowerNodeID);

    //this function only for communicate.
    std::map<long, long> GetMyFollowerMap();

    size_t GetMyFollowerCount();

    public long getMyNodeId() {
        return myNodeId;
    }

    public int getMajorityCount() {
        return 0;
    }

    public int getNodeCount() {
    }

    public int getPrepareTimeoutMs() {
    }

    public int getAcceptTimeoutMs() {
    }

    public boolean isValidNodeId(long nodeId) {
    }
}
