package cn.oomkiller.paxos4j.log.fs;

/**
 * save index in the memory
 */
public interface MemoryIndex {
    int getSize();
    void init();
    void insertIndexCache(long key, long value);
    long get(long key);
    long[] getKeys();
    long[] getOffsets();
}
