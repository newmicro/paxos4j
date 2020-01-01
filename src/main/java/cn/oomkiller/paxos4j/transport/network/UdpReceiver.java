package cn.oomkiller.paxos4j.transport.network;


public class UdpReceiver  extends Thread {
    private int sockFd;
    private boolean isEnd;
    private boolean isStarted;
    private MessageHandler handler;

    public UdpReceiver(final int port) {

    }

    public void setMessageHandler(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        isStarted = true;

//        char sBuffer[65536] = {0};
//
//        struct sockaddr_in addr;
//        socklen_t addr_len = sizeof(struct sockaddr_in);
//        memset(&addr, 0, sizeof(addr));
//
//        while(true) {
//            if (isEnd) {
//                return;
//            }
//
//            struct pollfd fd;
//            int ret;
//
//            fd.fd = m_iSockFD;
//            fd.events = POLLIN;
//            ret = poll(&fd, 1, 500);
//
//            if (ret == 0 || ret == -1)
//            {
//                continue;
//            }
//
//            int iRecvLen = recvfrom(m_iSockFD, sBuffer, sizeof(sBuffer), 0,
//                    (struct sockaddr *)&addr, &addr_len);
//
//            if (iRecvLen > 0)
//            {
//                m_poDFNetWork->onReceiveMessage(sBuffer, iRecvLen);
//            }
//        }
    }

    public void end() {
        isEnd = true;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
