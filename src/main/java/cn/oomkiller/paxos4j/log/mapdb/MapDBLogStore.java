package cn.oomkiller.paxos4j.log.mapdb;

import cn.oomkiller.paxos4j.log.LogStore;
import cn.oomkiller.paxos4j.utils.Constant;
import cn.oomkiller.paxos4j.utils.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.Map;

@Slf4j
public class MapDBLogStore implements LogStore {
  private static final int THREAD_NUM = 1;
  // partition num
  private final int groupNum = Constant.GROUP_NUM;
  // data
  public volatile DB[] databases;
  // index
  private volatile BTreeMap<byte[], byte[]>[] bTreeMaps;
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
    int partition = 0;
    return bTreeMaps[partition].get(DataUtil.longToBytes(instanceId));
  }

  @Override
  public void put(long instanceId, byte[] value, boolean isSync) {
    int partition = 0;
    bTreeMaps[partition].put(DataUtil.longToBytes(instanceId), value);
    log.info("btree map size" + bTreeMaps[partition].size());
  }

  @Override
  public long getMaxInstanceId() {
    int partition = 0;
    log.info("btree map size" + bTreeMaps[partition].size());
    if (bTreeMaps[partition].size() == 0) {
      return 0L;
    }
    byte[] bytes = bTreeMaps[partition].lastKey();
    return DataUtil.bytesToLong(bytes);
  }

  @Override
  public void setMinChosenInstanceId(long minInstanceId) {}

  @Override
  public long getMinChosenInstanceId() {
    return 0;
  }

  @Override
  public void clearAllLog() {
    for (BTreeMap map : bTreeMaps) {
      map.clear();
    }
  }

  @Override
  public void open(String logStoragePath) {
    if (logStoragePath.endsWith("/")) {
      logStoragePath = logStoragePath.substring(0, logStoragePath.length() - 1);
    }
    this.logStoragePath = logStoragePath;
    databases = new DB[groupNum];
    bTreeMaps = new BTreeMap[groupNum];

    for (int i = 0; i < groupNum; i++) {
      String dbFile = logStoragePath + Constant.DATA_PREFIX + i + Constant.DATA_SUFFIX;
      databases[i] = DBMaker.fileDB(dbFile).transactionEnable().make();
      Map map = databases[i].hashMap("map" + i).createOrOpen();
    }
    for (int i = 0; i < groupNum; i++) {
      bTreeMaps[i] =
          databases[i]
              .treeMap("treemap" + i)
              .keySerializer(Serializer.BYTE_ARRAY)
              .valueSerializer(Serializer.BYTE_ARRAY)
              .createOrOpen();
    }
  }

  @Override
  public void close() {
    if (databases != null) {
      for (DB db : databases) {
        db.close();
      }
    }
  }
}
