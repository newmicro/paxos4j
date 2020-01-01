package cn.oomkiller.paxos4j.log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLogStore implements LogStorage {
    private Path logDirPath;

    public FileLogStore(String logDir) {
        this.logDirPath = Paths.get(logDir);
        if (!logDirPath.toFile().exists()) {
            try {
                logDirPath.toFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getLogStorageDirPath() {
        return this.logDirPath.toAbsolutePath().toString();
    }

    @Override
    public byte[] get(long instanceId) {
        return new byte[0];
    }

    @Override
    public void put(long instanceId, byte[] value) {

    }

    @Override
    public void del(long instanceId) {

    }

    @Override
    public long getMaxInstanceId() {
        return 0;
    }

    @Override
    public void setMinChosenInstanceId(long minInstanceId) {

    }

    @Override
    public long getMinChosenInstanceId() {
        return 0;
    }

    @Override
    public void clearAllLog() {

    }
}
