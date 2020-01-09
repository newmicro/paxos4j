package cn.oomkiller.paxos4j.transport;

import cn.oomkiller.paxos4j.message.PaxosMsg;

public interface MsgTransport {
    void sendMessage(long sendToNodeId, PaxosMsg paxosMsg);

    void broadcastMessageWithoutCurrentNode(PaxosMsg paxosMsg);

    void broadcastMessageBeforeCurrentNode(PaxosMsg paxosMsg);

    void broadcastMessageAfterCurrentNode(PaxosMsg paxosMsg);
}
