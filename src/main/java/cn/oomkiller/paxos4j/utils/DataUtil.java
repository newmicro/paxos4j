package cn.oomkiller.paxos4j.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DataUtil {
  public static final int bytesToInt(byte[] bytes) {
    int value = 0;
    int len = 4;
    for (int i = 0; i < len; ++i) {
      value <<= 8;
      value |= (bytes[i] & 0xff);
    }
    return value;
  }

  public static byte[] intToBytes(int value) {
    byte[] buffer = new byte[4];
    for (int i = 0; i < 4; ++i) {
      int offset = 32 - (i + 1) * 8;
      buffer[i] = (byte) ((value >> offset) & 0xff);
    }
    return buffer;
  }

  public static final long bytesToLong(byte[] bytes) {
    long value = 0;
    int len = 8;
    for (int i = 0; i < len; ++i) {
      value <<= 8;
      value |= (bytes[i] & 0xff);
    }
    return value;
  }

  public static final void longToBytes(byte[] buffer, long value) {
    for (int i = 0; i < 8; ++i) {
      int offset = 64 - (i + 1) * 8;
      buffer[i] = (byte) ((value >> offset) & 0xff);
    }
  }

  public static final byte[] longToBytes(long value) {
    byte[] buffer = new byte[Constant.LONG_BYTE_LENGTH];
    for (int i = 0; i < 8; ++i) {
      int offset = 64 - (i + 1) * 8;
      buffer[i] = (byte) ((value >> offset) & 0xff);
    }
    return buffer;
  }
}
