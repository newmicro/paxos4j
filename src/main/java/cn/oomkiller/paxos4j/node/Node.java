package cn.oomkiller.paxos4j.node;

public interface Node {
    void runNode();

    void stopNode();

    boolean commitNewValue(byte[] value);

    long getMyNodeId();
}
