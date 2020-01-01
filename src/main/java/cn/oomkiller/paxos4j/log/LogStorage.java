package cn.oomkiller.paxos4j.log;

public interface LogStorage {

    String getLogStorageDirPath();

    byte[] get(long instanceId);

    void put(long instanceId, byte[] value);

    void del(long instanceId);

    long getMaxInstanceId();

    void setMinChosenInstanceId(long minInstanceId);

    long getMinChosenInstanceId();

    void clearAllLog();
}
