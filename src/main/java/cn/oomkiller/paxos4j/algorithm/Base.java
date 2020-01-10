package cn.oomkiller.paxos4j.algorithm;

import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import lombok.AllArgsConstructor;

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
