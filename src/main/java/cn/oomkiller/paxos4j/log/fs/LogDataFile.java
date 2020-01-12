package cn.oomkiller.paxos4j.log.fs;

import cn.oomkiller.paxos4j.utils.Constant;
import cn.oomkiller.paxos4j.utils.DataUtil;
import cn.oomkiller.paxos4j.utils.UnsafeUtil;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LogDataFile {
  // buffer
  private static ThreadLocal<ByteBuffer> reusableBuffer =
      ThreadLocal.withInitial(() -> ByteBuffer.allocate(Constant.MAX_VALUE_LENGTH));
  private FileChannel fileChannel;
  private ByteBuffer writeBuffer;
  /** we want to use {@link sun.misc.Unsafe} to copy memory, */
  private long writeBufferAddress;
  /** data offset */
  private long wrotePosition = 0L;
  /** buffer write pointer */
  private int bufferPosition;

  public void setWrotePosition(long lastEntryOffset) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    fileChannel.read(buffer, lastEntryOffset);
    int dataLength = DataUtil.bytesToInt(buffer.array());
    this.wrotePosition = lastEntryOffset + dataLength;
  }

  public void init(String logDir, int index) throws IOException {
    File dirFile = new File(logDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }
    File dataFile = new File(logDir + Constant.DATA_PREFIX + index + Constant.DATA_SUFFIX);
    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }
    fileChannel = new RandomAccessFile(dataFile, "rw").getChannel();
    writeBuffer = ByteBuffer.allocateDirect(Constant.OPTIMAL_WRITE_SIZE);

    writeBufferAddress = ((DirectBuffer) this.writeBuffer).address();
    bufferPosition = 0;
  }

  public byte[] read(long offset) throws IOException {
    ByteBuffer buffer = reusableBuffer.get();
    // read 4 bytes data length
    buffer.clear();
    buffer.limit(4);
    fileChannel.read(buffer, offset);
    int dataLength = DataUtil.bytesToInt(buffer.array());
    // read data bytes
    buffer.clear();
    buffer.limit(dataLength);
    fileChannel.read(buffer, offset + 4);
    return buffer.array();
  }

  public long write(byte[] data) {
    int initialMetaSize = 4;
    int initialDataSize = data.length;
    int metaSize = initialMetaSize;
    int totalSize = initialDataSize + initialMetaSize;
    long curPosition = wrotePosition;
    wrotePosition += totalSize;

    byte[] lengthBytes = DataUtil.intToBytes(data.length);
    while (totalSize > 0) {
      int remainSize = Constant.OPTIMAL_WRITE_SIZE - bufferPosition;
      int copySize = remainSize < totalSize ? remainSize : totalSize;
      int copyMetaSize = metaSize < copySize ? metaSize : copySize;
      int copyDataSize = copySize - copyMetaSize;
      // copy meta bytes
      if (copyMetaSize > 0) {
        int offset = initialMetaSize - metaSize;
        UnsafeUtil.copyToDirectMemory(
            lengthBytes, writeBufferAddress + bufferPosition, offset, metaSize);
        metaSize -= copyMetaSize;
        totalSize -= copyMetaSize;
        bufferPosition += copyMetaSize;
      }
      // copy data bytes
      if (copyDataSize > 0) {
        int offset = initialDataSize - totalSize;
        UnsafeUtil.copyToDirectMemory(data, writeBufferAddress + bufferPosition, offset, copySize);
        totalSize -= copySize;
        bufferPosition += copySize;
      }
      if (bufferPosition >= Constant.OPTIMAL_WRITE_SIZE) {
        writeBuffer.position(0);
        writeBuffer.limit(bufferPosition);
        try {
          fileChannel.write(writeBuffer);
        } catch (IOException e) {
          //          throw new EngineException(RetCodeEnum.IO_ERROR, "fileChannel write data io
          // error");
        }
        bufferPosition = 0;
      }
    }

    return curPosition;
  }

  public void destroy() throws IOException {
    if (bufferPosition > 0) {
      writeBuffer.position(0);
      writeBuffer.limit(bufferPosition);
      fileChannel.write(writeBuffer);
      bufferPosition = 0;
    }
    writeBuffer = null;
    if (fileChannel != null) {
      fileChannel.close();
    }
  }
}
