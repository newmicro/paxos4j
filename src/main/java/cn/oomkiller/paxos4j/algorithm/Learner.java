package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.statemachine.StateMachine;
import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.log.LogStorage;
import cn.oomkiller.paxos4j.log.PaxosLog;
import cn.oomkiller.paxos4j.transport.MsgTransport;

public class Learner extends Base {


    public Learner(Config config,
                   MsgTransport msgTransport,
                   Acceptor acceptor,
                   LogStorage poLogStorage,
                   StateMachine stateMachine) {
        super(config, msgTransport);
    }

    @Override
    public void initForNewPaxosInstance() {

    }

    public void resetAskforLearnNoop() {
    }

    public void proposerSendSuccess(long instanceId, long proposalId) {
    }

    public void startLearnerSender() {
    }

    public void stop() {
    }

    public long getSeenLatestInstanceId() {
        return 0;
    }


    static class State {
        private byte[] learnedValue;
        private boolean learned;
        private Config config;
        private PaxosLog paxosLog;


        State(Config config, LogStorage logStorage) {
            this.config = config;
            this.paxosLog = new PaxosLog(logStorage);
        }

        void init() {
            learned = false;
            learnedValue = null;
        }

        byte[] learnValue(long instanceId, BallotNumber learnedBallot) {
            return null;
        }

        byte[] learnValueWithoutWrite(long instanceId, BallotNumber learnedBallot) {
            return null;
        }

        byte[] getLearnValue() {
            return learnedValue;
        }

        boolean getIsLearned() {
            return true;
        }
    }
}
