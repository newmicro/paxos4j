package cn.oomkiller.paxos4j.log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileLogStore implements LogStorage {
  private Path logPath;

  public FileLogStore(String logDir) {
    this.logPath = Paths.get(logDir);
    log.info(this.logPath.toAbsolutePath().toString());
    if (!logPath.toFile().exists()) {
      try {
        logPath.toFile().createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String getLogStorageDirPath() {
    return this.logPath.toAbsolutePath().toString();
  }

  @Override
  public byte[] get(long instanceId) {
    return new byte[0];
  }

  @Override
  public void put(long instanceId, byte[] value) {}

  @Override
  public void delete(long instanceId) {}

  @Override
  public long getMaxInstanceId() {
    return 0;
  }

  @Override
  public void setMinChosenInstanceId(long minInstanceId) {}

  @Override
  public long getMinChosenInstanceId() {
    return 0;
  }

  @Override
  public void clearAllLog() {}
}
