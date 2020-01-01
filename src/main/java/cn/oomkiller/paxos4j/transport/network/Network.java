package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import cn.oomkiller.paxos4j.transport.DefaultMessageHandler;

import java.net.SocketAddress;

public interface Network {

    void init(SocketAddress address, int ioThreadCount);

    void runNetwork();

    void stopNetwork();

    void sendMessageTCP(SocketAddress address, Message message);

    void sendMessageUDP(SocketAddress address, byte[] message);

    void setMessageHandler(DefaultMessageHandler defaultMessageHandler);
}
