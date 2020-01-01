package cn.oomkiller.paxos4j.config;

import lombok.Data;

@Data
public class NodeInfo {
    private long nodeId;
    private String ip;
    private int port;

    public NodeInfo(long sendToNodeId) {

    }
}
