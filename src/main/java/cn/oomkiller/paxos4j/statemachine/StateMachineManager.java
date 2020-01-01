package cn.oomkiller.paxos4j.statemachine;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class StateMachineManager {

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
