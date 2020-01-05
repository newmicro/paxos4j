package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.utils.IpUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NodeInfo {
  private long nodeId;
  private String ip;
  private int port;

  public NodeInfo(long nodeId) {
    setNodeId(nodeId);
  }

  public NodeInfo(String ip, int port) {
    setIpPort(ip, port);
  }

  public void setIpPort(String ip, int port) {
    this.ip = ip;
    this.port = port;
    makeNodeId();
  }

  public void setNodeId(long nodeId) {
    this.nodeId = nodeId;
    parseNodeId();
  }

  private void makeNodeId() {
    long ipNumber = IpUtil.ipToLong(this.ip);
    assert (ipNumber != -1L);

    this.nodeId = (ipNumber << 32) | this.port;
  }

  private void parseNodeId() {
    this.port = (int) (this.nodeId & (0xffffffff));
    this.ip = IpUtil.longToIp(this.nodeId >> 32);
  }
}
