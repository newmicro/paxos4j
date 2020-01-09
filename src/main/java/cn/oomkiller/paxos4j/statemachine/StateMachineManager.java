package cn.oomkiller.paxos4j.statemachine;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class StateMachineManager {
    private final List<StateMachine> stateMachineList = new LinkedList<>();

    public void addStateMachine(StateMachine stateMachine) {
        try {
            FileChannel.open(Paths.get("/abc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(long instanceId, byte[] value, StateMachineContext ctx) {

    }
}
