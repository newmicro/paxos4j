package cn.oomkiller.paxos4j.utils;

import java.util.Random;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomUtil {
  private static final Random RANDOM = new Random();

  public static final int randomInt() {
    return RANDOM.nextInt();
  }

  public static final int randomInt(int bound) {
    return RANDOM.nextInt(bound);
  }
}
