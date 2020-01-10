package cn.oomkiller.paxos4j.log;

public interface LogStore {

    String getLogStorePath();

    byte[] get(long instanceId);

    void put(long instanceId, byte[] value, boolean isSync);

    void delete(long instanceId);

    long getMaxInstanceId();

    void setMinChosenInstanceId(long minInstanceId);

    long getMinChosenInstanceId();

    void clearAllLog();
}
