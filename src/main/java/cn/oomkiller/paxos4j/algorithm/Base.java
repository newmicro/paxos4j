package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public abstract class Base {
    protected Config config;
    protected MsgTransport msgTransport;

    private long instanceId;

    public Base(Config config, MsgTransport msgTransport) {
        this.config = config;
        this.msgTransport = msgTransport;
        this.instanceId = 0L;
    }

    public long getInstanceId() {
        return this.instanceId;
    }

    protected void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public void newInstance() {
        this.instanceId++;
        initForNewPaxosInstance();
    }

    public abstract void initForNewPaxosInstance();
}


@Getter
@Setter
class BallotNumber {
    private long proposalId;
    private long nodeId;

    public BallotNumber() {
        this.proposalId = 0L;
        this.nodeId = 0L;
    }

    public BallotNumber(long proposalId, long nodeId) {
        this.proposalId = proposalId;
        this.nodeId = nodeId;
    }

    public boolean greaterThanOrEqual(BallotNumber other) {
        if (this.proposalId == other.proposalId) {
            return this.nodeId >= other.nodeId;
        } else {
            return this.proposalId >= other.proposalId;
        }
    }

    public boolean notEqual(BallotNumber other) {
        return this.proposalId != other.proposalId
                || this.nodeId != other.nodeId;
    }

    public boolean equal(BallotNumber other) {
        return this.proposalId == other.proposalId
                && this.nodeId == other.nodeId;
    }

    public boolean greaterThan(BallotNumber other) {
        if (this.proposalId == other.proposalId) {
            return this.nodeId > other.nodeId;
        } else {
            return this.proposalId > other.proposalId;
        }
    }

    public boolean isNull() {
        return this.proposalId == 0L;
    }

    public boolean notNull() {
        return this.proposalId > 0L;
    }

    public void reset() {
        this.proposalId = 0L;
        this.nodeId = 0L;
    }
}
