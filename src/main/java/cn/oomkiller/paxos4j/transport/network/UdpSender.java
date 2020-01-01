package cn.oomkiller.paxos4j.transport.network;

import java.net.SocketAddress;
import java.util.List;

public class UdpSender extends Thread {
    private int sockFd;
    private boolean isEnd;
    private boolean isStarted;
    private List<QueueData> sendQueue;

    void addMessage(SocketAddress destination, byte[] message) {

    }

    @Override
    public void run() {

    }

    public void end() {

    }

    static class QueueData {
        String ip;
        int port;
        byte[] message;
    }
}
