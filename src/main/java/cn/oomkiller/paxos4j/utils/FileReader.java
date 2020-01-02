package cn.oomkiller.paxos4j.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

@UtilityClass
public class FileReader {
  public static String readContentFromClassPath(String path) throws IOException {
    String result;
    try (InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
      result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
    return result;
  }

  public static String readContentFromFilePath(String path) throws IOException {
    String result;
    try (InputStream inputStream = new FileInputStream(path)) {
      result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
    return result;
  }
}
