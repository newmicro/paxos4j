package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;

public interface MessageHandler {
    void onReceiveMessage(Message message);
}
