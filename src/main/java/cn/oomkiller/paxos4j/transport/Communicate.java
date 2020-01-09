package cn.oomkiller.paxos4j.transport;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.config.NodeInfo;
import cn.oomkiller.paxos4j.message.Message;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.transport.network.Network;
import cn.oomkiller.paxos4j.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Communicate implements MsgTransport {
  private Config config;
  private Network network;

  long myNodeId;
  long udpMaxSize;

  public Communicate(Config config, Network network) {
    this.config = config;
    this.network = network;
    this.myNodeId = config.getMyNodeId();
  }

  private SocketAddress getSocketAddressFromNodeId(long nodeId) {
    NodeInfo targetNode = new NodeInfo(nodeId);
    return new InetSocketAddress(targetNode.getIp(), targetNode.getPort());
  }

  public void sendMessage(long sendToNodeId, PaxosMsg paxosMsg) {
    Message<PaxosMsg> message = new Message<>(IdUtil.nextId(), paxosMsg);
    network.sendMessageTCP(getSocketAddressFromNodeId(sendToNodeId), message);
  }

  @Override
  public void broadcastMessageWithoutCurrentNode(PaxosMsg paxosMsg) {
    Set<Long> nodeIds =
        new HashSet<>(
            config.getNodeInfoList().stream()
                .map(NodeInfo::getNodeId)
                .collect(Collectors.toList()));

    for (long nodeId : nodeIds) {
      if (nodeId != myNodeId) {
        Message<PaxosMsg> message = new Message<>(IdUtil.nextId(), paxosMsg);
        network.sendMessageTCP(getSocketAddressFromNodeId(nodeId), message);
      }
    }
  }

  public void broadcastMessageAfterCurrentNode(PaxosMsg paxosMsg) {
    Message<PaxosMsg> message = new Message<>(IdUtil.nextId(), paxosMsg);
    network.sendMessageTCP(getSocketAddressFromNodeId(config.getMyNodeId()), message);
    broadcastMessageWithoutCurrentNode(paxosMsg);
  }

  public void broadcastMessageBeforeCurrentNode(PaxosMsg paxosMsg) {
    broadcastMessageWithoutCurrentNode(paxosMsg);
    Message<PaxosMsg> message = new Message<>(IdUtil.nextId(), paxosMsg);
    network.sendMessageTCP(getSocketAddressFromNodeId(config.getMyNodeId()), message);
  }
}
