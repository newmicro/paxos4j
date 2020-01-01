package cn.oomkiller.paxos4j.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public final class IdUtil {
  private static final AtomicLong IDX = new AtomicLong();

  public static long nextId() {
    return IDX.incrementAndGet();
  }
}
