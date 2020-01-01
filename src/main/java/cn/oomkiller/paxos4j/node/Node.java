package cn.oomkiller.paxos4j.node;

import cn.oomkiller.paxos4j.config.Options;

public interface Node {
    void runNode(Options options);

    void stopNode();

    //Base function.
    boolean propose(byte[] value);

    long getMyNodeId();
}
