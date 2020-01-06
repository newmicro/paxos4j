package cn.oomkiller.paxos4j.examples;

import cn.oomkiller.paxos4j.config.Options;
import cn.oomkiller.paxos4j.node.Node;
import java.io.IOException;

public class AbstractPaxosServer {
    private Node node;

    public void runPaxos() throws IOException {
        Options.load("");
    }
}
