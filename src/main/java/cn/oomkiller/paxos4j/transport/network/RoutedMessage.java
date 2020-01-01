package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import lombok.AllArgsConstructor;

import java.net.SocketAddress;

@AllArgsConstructor
public class RoutedMessage {
    SocketAddress destination;
    Message payload;
}
