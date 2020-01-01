package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.message.PaxosMsg;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class IOLoop implements Runnable {
    private static final int RETRY_QUEUE_MAX_LEN = 1000;
    private boolean isEnd;
    private boolean isStart;
//    private Timer timer;
    private Map<Integer, Boolean> mapTimerIdExist;

    private BlockingQueue<byte[]> messageQueue;
    private BlockingQueue<PaxosMsg> retryQueue;

    private int queueMemSize;

    private Config config;
    private Instance instance;

    public IOLoop(Config config, Instance instance) {
        this.config = config;
        this.instance = instance;

        this.isEnd = false;
        this.isStart = false;

        this.queueMemSize = 0;
    }

    @Override
    public void run() {
        isEnd = false;
        isStart = true;
        while(true) {
            int iNextTimeout = 1000;

            dealWithTimeout(iNextTimeout);

            oneLoop(iNextTimeout);

            if (isEnd) {
                break;
            }
        }
    }

    void stop() {
//        isEnd = true;
//        if (isStart) {
//            join();
//        }
    }

    void oneLoop(int timeoutMs) {

    }

    void dealWithRetry() {
        if (retryQueue.isEmpty()) {
            return;
        }

        boolean haveRetryOne = false;
        while (!retryQueue.isEmpty())
        {
            PaxosMsg paxosMsg = retryQueue.peek();
            if (paxosMsg.getInstanceId() > instance.getNowInstanceId() + 1) {
                break;
            } else if (paxosMsg.getInstanceId() == instance.getNowInstanceId() + 1) {
                //only after retry i == now_i, than we can retry i + 1.
                if (haveRetryOne) {
                    instance.onReceivePaxosMsg(paxosMsg, true);
                } else {
                    break;
                }
            } else if (paxosMsg.getInstanceId() == instance.getNowInstanceId()) {
                instance.onReceivePaxosMsg(paxosMsg, false);
                haveRetryOne = true;
            }

            retryQueue.poll();
        }
    }

    void clearRetryQueue() {
        while (!retryQueue.isEmpty()) {
            retryQueue.poll();
        }
    }

    public void addMessage(byte[] message, int messageLen) {
//        if (messageQueue.size() > QUEUE_MAXLENGTH) {
//            PLGErr("Queue full, skip msg");
//            messageQueue.unlock();
//        }
//
//        if (queueMemSize > MAX_QUEUE_MEM_SIZE) {
//            messageQueue.unlock();
//        }
//
//        messageQueue.add(new string(pcMessage, iMessageLen));
//        queueMemSize += iMessageLen;
    }

    void addRetryPaxosMsg(PaxosMsg paxosMsg) {
        if (retryQueue.size() > RETRY_QUEUE_MAX_LEN) {
            retryQueue.poll();
        }

        retryQueue.offer(paxosMsg);
    }

    void addNotify() {
        messageQueue.add(null);
    }

    public boolean addTimer(int iTimeout, int iType, int iTimerID) {
//        if (iTimeout == -1) {
//            return true;
//        }
//
//        long llAbsTime = Time::GetSteadyClockMS() + iTimeout;
//        timer.addTimerWithType(llAbsTime, iType, iTimerID);
//
//        mapTimerIdExist[iTimerID] = true;

        return true;
    }

    void removeTimer(int iTimerID) {

    }

    void dealWithTimeout(int iNextTimeout) {
//        boolean bHasTimeout = true;
//
//        while(bHasTimeout) {
//            int iTimerID = 0;
//            int iType = 0;
//            bHasTimeout = timer.popTimeout(iTimerID, iType);
//
//            if (bHasTimeout) {
//                dealwithTimeoutOne(iTimerID, iType);
//
//                iNextTimeout = timer.getNextTimeout();
//                if (iNextTimeout != 0) {
//                    break;
//                }
//            }
//        }
    }

//    void dealwithTimeoutOne(uint32_t iTimerID, int iType) {
//        auto it = m_mapTimerIDExist.find(iTimerID);
//        if (it == end(m_mapTimerIDExist)) {
//            return;
//        }
//
//        m_mapTimerIDExist.erase(it);
//        instance.onTimeout(iTimerID, iType);
//    }

    public void start() {
    }
}
