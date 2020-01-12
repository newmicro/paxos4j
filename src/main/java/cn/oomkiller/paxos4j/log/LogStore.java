package cn.oomkiller.paxos4j.log;

public interface LogStore {

    String getLogStorePath();

    byte[] get(long instanceId);

    void put(long instanceId, byte[] value, boolean isSync);

    long getMaxInstanceId();

    void setMinChosenInstanceId(long minInstanceId);

    long getMinChosenInstanceId();

    void clearAllLog();

    void open(String logStoragePath);

    void close();
}
