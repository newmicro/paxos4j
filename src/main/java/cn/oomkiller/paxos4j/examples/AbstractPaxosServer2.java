package cn.oomkiller.paxos4j.examples;

import cn.oomkiller.paxos4j.config.Options;
import cn.oomkiller.paxos4j.node.Node;
import cn.oomkiller.paxos4j.node.PaxosNode;
import java.io.IOException;

public class AbstractPaxosServer2 {
    private Node node;

    public void runPaxos() throws IOException, InterruptedException {
        Options options = Options.load("conf/config2.yaml");
        node = new PaxosNode(options);
        node.runNode();

        int cnt = 0;
        while (true) {
            Thread.sleep(10*1000);
//            node.commitNewValue(("Hello" + System.currentTimeMillis()).getBytes());
        }
    }

  public static void main(String[] args) throws IOException, InterruptedException {
    //
      AbstractPaxosServer2 server = new AbstractPaxosServer2();
      server.runPaxos();
  }
}
