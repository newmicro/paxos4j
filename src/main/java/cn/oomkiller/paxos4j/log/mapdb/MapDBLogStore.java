package cn.oomkiller.paxos4j.log.mapdb;

import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.log.fs.LogDataFile;
import cn.oomkiller.paxos4j.log.fs.LogIndexFile;
import cn.oomkiller.paxos4j.log.fs.MemoryIndex;
import cn.oomkiller.paxos4j.utils.Constant;
import cn.oomkiller.paxos4j.utils.DataUtil;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapDBLogStore implements LogStore {
  private static final int THREAD_NUM = 1;
  // partition num
  private final int groupNum = Constant.GROUP_NUM;
  // key -> partition
  //  private volatile Partitionable partitionable;
  // data
  public volatile LogDataFile[] logDataFiles;
  // index
  private volatile LogIndexFile[] logIndexFiles;
  // true means need to load index into memory, false means no need
  private volatile boolean needLoad = false;
  private String logStoragePath;

  public MapDBLogStore() {}

  @Override
  public String getLogStorePath() {
    return logStoragePath;
  }

  @Override
  public byte[] get(long instanceId) {
    return read(DataUtil.longToBytes(instanceId));
  }

  @Override
  public void put(long instanceId, byte[] value, boolean isSync) {
    write(DataUtil.longToBytes(instanceId), value);
  }

  @Override
  public long getMaxInstanceId() {
    int partition = 0;
    MemoryIndex hitMemoryIndex = logIndexFiles[partition].getMemoryIndex();
    return hitMemoryIndex.getKeys()[hitMemoryIndex.getSize() - 1];
  }

  @Override
  public void setMinChosenInstanceId(long minInstanceId) {}

  @Override
  public long getMinChosenInstanceId() {
    return 0;
  }

  @Override
  public void clearAllLog() {}

  @Override
  public void open(String logStoragePath) {
    if (logStoragePath.endsWith("/")) {
      logStoragePath = logStoragePath.substring(0, logStoragePath.length() - 1);
    }
    this.logStoragePath = logStoragePath;
    logDataFiles = new LogDataFile[groupNum];
    logIndexFiles = new LogIndexFile[groupNum];
    try {
      for (int i = 0; i < groupNum; i++) {
        logDataFiles[i] = new LogDataFile();
        logDataFiles[i].init(logStoragePath, i);
      }
      for (int i = 0; i < groupNum; i++) {
        logIndexFiles[i] = new LogIndexFile();
        logIndexFiles[i].init(logStoragePath, i);
        logIndexFiles[i].setLogDataFile(logDataFiles[i]);
        needLoad = needLoad || (!logIndexFiles[i].isLoaded());
      }
      //      if (needLoad) {
      loadAllIndex();
      //      }
    } catch (IOException e) {
      throw new RuntimeException("open exception");
    }
  }

  @Override
  public void close() {
    if (logDataFiles != null) {
      for (LogDataFile dataFile : logDataFiles) {
        try {
          dataFile.destroy();
        } catch (IOException e) {
          log.error("data destroy error", e);
        }
      }
    }
    if (logIndexFiles != null) {
      for (LogIndexFile indexFile : logIndexFiles) {
        try {
          indexFile.destroy();
        } catch (IOException e) {
          log.error("data destroy error", e);
        }
      }
    }
  }

  private byte[] read(byte[] key) {
    int partition = 0; // partitionable.getPartition(key);
    LogDataFile hitDataFile = logDataFiles[partition];
    LogIndexFile hitIndexFile = logIndexFiles[partition];
    long offset = hitIndexFile.read(key);
    if (offset < 0) {
      return null;
      //      throw new EngineException(RetCodeEnum.NOT_FOUND, Util.bytes2Long(key) + " not found");
    }
    try {
      return hitDataFile.read(offset);
    } catch (IOException e) {
      return null;
      //      throw new EngineException(RetCodeEnum.IO_ERROR, "commit log read exception");
    }
  }

  private void write(byte[] key, byte[] value) {
    int partition = 0; // partitionable.getPartition(key);
    LogDataFile hitDataFile = logDataFiles[partition];
    LogIndexFile hitIndexFile = logIndexFiles[partition];
    synchronized (hitDataFile) {
      long offset = hitDataFile.write(value);
      hitIndexFile.write(key, offset);
    }
  }

  private void loadAllIndex() {
    int loadThreadNum = THREAD_NUM;
    CountDownLatch countDownLatch = new CountDownLatch(loadThreadNum);
    for (int i = 0; i < loadThreadNum; i++) {
      final int index = i;
      new Thread(
              () -> {
                for (int partition = 0; partition < groupNum; partition++) {
                  if (partition % loadThreadNum == index) {
                    try {
                      logIndexFiles[partition].load();
                    } catch (IOException e) {
                      log.error("Failed load index file #" + partition, e);
                    }
                  }
                }
                countDownLatch.countDown();
              })
          .start();
    }
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      log.error("load index interrupted", e);
    }
    needLoad = false;
  }
}
