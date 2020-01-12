package cn.oomkiller.paxos4j.utils;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@UtilityClass
public class UnsafeUtil {
  public static final Unsafe UNSAFE;

  static {
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void copyToDirectMemory(byte[] data, long directAddress, int length) {
    UNSAFE.copyMemory(data, 16, null, directAddress, length);
  }

  public static void copyToDirectMemory(byte[] data, long directAddress, int offset, int length) {
    UNSAFE.copyMemory(data, 16 + offset, null, directAddress, length);
  }
}
