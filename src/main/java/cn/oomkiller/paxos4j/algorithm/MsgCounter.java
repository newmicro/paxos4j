package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import java.util.Set;

public class MsgCounter {
    private Config config;

    private Set<Long> receiveMsgNodeIds;
    private Set<Long> rejectMsgNodeIds;
    private Set<Long> promiseOrAcceptMsgNodeIds;

    public MsgCounter(Config config) {
        this.config = config;
        startNewRound();
    }

    void addReceive(long nodeId) {
        receiveMsgNodeIds.add(nodeId);
    }

    void addReject(long nodeId) {
        rejectMsgNodeIds.add(nodeId);
    }

    void addPromiseOrAccept(long nodeId) {
        promiseOrAcceptMsgNodeIds.add(nodeId);
    }

    boolean isPassedOnThisRound() {
        return promiseOrAcceptMsgNodeIds.size() >= config.getMajorityCount();
    }
    boolean isRejectedOnThisRound() {
        return rejectMsgNodeIds.size() >= config.getMajorityCount();
    }
    boolean isAllReceiveOnThisRound(){
        return receiveMsgNodeIds.size() == config.getNodeCount();
    }

    void startNewRound() {
        receiveMsgNodeIds.clear();
        rejectMsgNodeIds.clear();
        promiseOrAcceptMsgNodeIds.clear();
    }
}
