package cn.oomkiller.paxos4j.utils;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JsonUtil {

  private static final Gson GSON = new Gson();

  public static <T> T fromJson(String jsonStr, Class<T> clazz) {
    return GSON.fromJson(jsonStr, clazz);
  }

  public static String toJson(Object object) {
    return GSON.toJson(object);
  }
}
