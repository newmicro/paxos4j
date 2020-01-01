package cn.oomkiller.paxos4j.transport;

import cn.oomkiller.paxos4j.algorithm.Instance;
import cn.oomkiller.paxos4j.message.Message;
import cn.oomkiller.paxos4j.message.MessageBody;
import cn.oomkiller.paxos4j.message.PaxosMsg;
import cn.oomkiller.paxos4j.transport.network.MessageHandler;

public class DefaultMessageHandler implements MessageHandler {
  private final Instance instance;

  public DefaultMessageHandler(Instance instance) {
    this.instance = instance;
  }

  @Override
  public void onReceiveMessage(Message message) {
    MessageBody payload = message.getBody();
    if (payload instanceof PaxosMsg) {
      PaxosMsg paxosMsg = (PaxosMsg) payload;
      instance.onReceivePaxosMsg(paxosMsg, false);
    }
  }
}
