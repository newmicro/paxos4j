package cn.oomkiller.paxos4j.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
  public static final String DATA_PREFIX = "/data";
  public static final String DATA_SUFFIX = ".paxos";
  public static final String INDEX_PREFIX = "/index";
  public static final String INDEX_SUFFIX = ".paxos";
  public static int LONG_BYTE_LENGTH = 8;
  public static int IDS_DATA_NUM = 5;
  public static int IDS_DATA_LENGTH = IDS_DATA_NUM * LONG_BYTE_LENGTH;
  public static final int MAX_VALUE_LENGTH = 4 * 1024;
  public static final int INDEX_LENGTH = 2 * LONG_BYTE_LENGTH;
  public static final int EXPECTED_NUM_PER_GROUP = 64000;
  public static final int MAX_NUM_PER_GROUP = 128000;
  public static final int _4KB = 4 * 1024;
  public static final int _4MB = _4KB * 1024;
  public static final int OPTIMAL_WRITE_SIZE = 4 * _4KB;

  public static int GROUP_NUM = 1;

//  public static DirectIOLib directIOLib = DirectIOLib.getLibForPath("test_directory");
}
