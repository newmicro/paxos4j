package cn.oomkiller.paxos4j.utils;

import java.io.IOException;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

@UtilityClass
public class YamlUtil {
  private static final Yaml YAML = new Yaml();

  public static final <T> T loadAs(String filePath, Class<T> type) throws IOException {
    String yaml = FileReader.readContentFromFilePath(filePath);
    return YAML.loadAs(yaml, type);
  }

}
