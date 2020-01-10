package cn.oomkiller.paxos4j.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileLogStore implements LogStore {
  private File logFile;

  public FileLogStore(String logStoragePath) {
    Path logPath = Paths.get(logStoragePath);
    logFile = logPath.toFile();
    log.info(logPath.toAbsolutePath().toString());
    if (!logPath.toFile().exists()) {
      try {
        logPath.toFile().createNewFile();
      } catch (IOException e) {
        log.error("Create paxos log file failed!", e);
      }
    }
  }

  @Override
  public String getLogStorePath() {
    return logFile.toPath().toString();
  }

  @Override
  public byte[] get(long instanceId) {

    return new byte[0];
  }

  @Override
  public void put(long instanceId, byte[] value, boolean isSync) {
    if (isSync) {

    }
  }

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
  public void clearAllLog() {

  }
}
