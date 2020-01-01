package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import cn.oomkiller.paxos4j.transport.DefaultMessageHandler;

import java.net.SocketAddress;

public class DefaultNetwork implements Network {
  private TcpServer tcpServer;
  private TcpClient tcpClient;
  private UdpReceiver udpReceiver;
  private UdpSender udpSender;

  public DefaultNetwork(String ip, int port, int ioThreadCount) {
    this.tcpServer = new TcpServer(ip, port, ioThreadCount);
    this.tcpClient = new TcpClient();
    this.udpReceiver = new UdpReceiver(port);
    this.udpSender = new UdpSender();
  }

  @Override
  public void init(SocketAddress address, int ioThreadCount) {}

  @Override
  public void runNetwork() {
    tcpServer.start();
    tcpClient.start();
    udpReceiver.start();
    udpSender.start();
  }

  @Override
  public void stopNetwork() {
    tcpServer.end();
    tcpClient.end();
    udpReceiver.end();
    udpSender.end();
  }

  @Override
  public void sendMessageTCP(SocketAddress address, Message message) {
    tcpClient.addMessage(address, message);
  }

  @Override
  public void sendMessageUDP(SocketAddress address, byte[] message) {
    udpSender.addMessage(address, message);
  }

  @Override
  public void setMessageHandler(DefaultMessageHandler handler) {
    tcpServer.setMessageHandler(handler);
    udpReceiver.setMessageHandler(handler);
  }
}
