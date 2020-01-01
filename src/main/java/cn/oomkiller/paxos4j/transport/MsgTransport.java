package cn.oomkiller.paxos4j.transport;

import cn.oomkiller.paxos4j.message.PaxosMsg;

public interface MsgTransport {
    void sendMessage(long sendToNodeId, PaxosMsg paxosMsg);

    void broadcastMessage(PaxosMsg paxosMsg);
}
