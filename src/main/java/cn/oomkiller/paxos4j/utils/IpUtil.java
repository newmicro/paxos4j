package cn.oomkiller.paxos4j.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IpUtil {
  private final static String DOT = ".";

  public final static long ipToLong(String ip) {
    String[] ipStrs = ip.split(DOT);
    if (ipStrs.length != 4) {
      throw new IllegalArgumentException("Wrong ip format: " + ip);
    }

    long ipNumber = 0L;
    int shift = 24;
    for (String ipStr : ipStrs) {
      ipNumber += Long.parseLong(ipStr) << shift;
      shift -= 8;
    }

    return ipNumber;
  }


  public final static String longToIp(long longIp) {
    StringBuffer sb = new StringBuffer();
    sb.append(longIp >>> 24);
    sb.append(DOT);
    sb.append((longIp & 0x00FFFFFF) >>> 16);
    sb.append(DOT);
    sb.append((longIp & 0x0000FFFF) >>> 8);
    sb.append(DOT);
    sb.append(longIp & 0x000000FF);
    return sb.toString();
  }
}
