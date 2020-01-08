package cn.oomkiller.paxos4j.examples;

import cn.oomkiller.paxos4j.config.Options;
import cn.oomkiller.paxos4j.node.Node;
import cn.oomkiller.paxos4j.node.PaxosNode;
import java.io.IOException;

public class AbstractPaxosServer {
    private Node node;

    public void runPaxos() throws IOException, InterruptedException {
        Options options = Options.load("conf/config.yaml");
        node = new PaxosNode(options);
        node.runNode();

        int cnt = 0;
        while (cnt++ < 10) {
            node.commitNewValue(("Hello" + System.currentTimeMillis()).getBytes());
            Thread.sleep(10*1000);
        }
    }

  public static void main(String[] args) throws IOException, InterruptedException {
    //
      AbstractPaxosServer server = new AbstractPaxosServer();
      server.runPaxos();
  }
}
