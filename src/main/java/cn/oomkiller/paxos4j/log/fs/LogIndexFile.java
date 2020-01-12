package cn.oomkiller.paxos4j.log.fs;

import cn.oomkiller.paxos4j.utils.Constant;
import cn.oomkiller.paxos4j.utils.DataUtil;
import cn.oomkiller.paxos4j.utils.MmapUtil;
import cn.oomkiller.paxos4j.utils.UnsafeUtil;
import lombok.extern.slf4j.Slf4j;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class LogIndexFile implements LogDataAware {
  // memory index dataStructure
  private MemoryIndex memoryIndex;
  private FileChannel fileChannel;
  private MappedByteBuffer mappedByteBuffer;
  // mmap byteBuffer start address
  private long address;
  // 当前索引写入的区域
  private LogDataFile logDataFile;
  // determine current index block is loaded into memory
  private volatile boolean loaded = false;
  private long wrotePosition;

  @Override
  public void setLogDataFile(LogDataFile logDataFile) {
    this.logDataFile = logDataFile;
  }

  public MemoryIndex getMemoryIndex() {
    return memoryIndex;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void init(String logDir, int index) throws IOException {
    File dirFile = new File(logDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
      loaded = true;
    }

    File indexFile = new File(logDir + Constant.INDEX_PREFIX + index + Constant.INDEX_SUFFIX);
    if (!indexFile.exists()) {
      indexFile.createNewFile();
      loaded = true;
    }

    fileChannel = new RandomAccessFile(indexFile, "rw").getChannel();
  }

  public void load() throws IOException {
    int indexSize = getIndexSize();
    memoryIndex = new ArrayMemoryIndex(indexSize);
    if (indexSize == 0) {
      return;
    }
    ByteBuffer buffer = ByteBuffer.allocateDirect(indexSize * Constant.INDEX_LENGTH);
    try {
      fileChannel.read(buffer);
    } catch (IOException e) {
      log.error("load index failed", e);
    }
    buffer.flip();
    long key, offset = 0;
    for (int index = 0; index < indexSize; index++) {
      buffer.position(index * Constant.INDEX_LENGTH);
      key = buffer.getLong();
      offset = buffer.getLong();
      memoryIndex.insertIndexCache(key, offset);
    }
    ((DirectBuffer) buffer).cleaner().clean();

    logDataFile.setWrotePosition(offset);
    memoryIndex.init();
    loaded = true;
  }

  private int getIndexSize() {
    try {
      return (int) (fileChannel.size() / Constant.INDEX_LENGTH);
    } catch (IOException e) {
      return 0;
    }
  }

  public long read(byte[] key) {
    return memoryIndex.get(DataUtil.bytesToLong(key));
  }

  public void write(byte[] key, long offset) {
    if (mappedByteBuffer == null) {
      try {
        mappedByteBuffer =
            fileChannel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                Constant.INDEX_LENGTH * Constant.EXPECTED_NUM_PER_GROUP);
      } catch (IOException e) {
        log.error("mmap failed", e);
      }
      address = ((DirectBuffer) mappedByteBuffer).address();
      wrotePosition = 0;
    }
    if (wrotePosition >= mappedByteBuffer.limit() - Constant.INDEX_LENGTH) {
      try {
        mappedByteBuffer =
            fileChannel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                Constant.INDEX_LENGTH * Constant.MAX_NUM_PER_GROUP);
      } catch (IOException e) {
        log.error("mmap failed", e);
      }
      address = ((DirectBuffer) mappedByteBuffer).address();
    }

    UnsafeUtil.copyToDirectMemory(key, address + wrotePosition, Constant.LONG_BYTE_LENGTH);
    byte[] offsetBytes = DataUtil.longToBytes(offset);
    UnsafeUtil.copyToDirectMemory(offsetBytes, address + wrotePosition, Constant.LONG_BYTE_LENGTH);
    wrotePosition += Constant.INDEX_LENGTH;

    memoryIndex.insertIndexCache(DataUtil.bytesToLong(key), offset);
  }

  public void destroy() throws IOException {
    logDataFile = null;
    loaded = false;
    releaseFile();
  }

  private void releaseFile() throws IOException {
    if (mappedByteBuffer != null) {
      fileChannel.close();
      MmapUtil.clean(mappedByteBuffer);
      fileChannel = null;
      mappedByteBuffer = null;
    }
  }
}
