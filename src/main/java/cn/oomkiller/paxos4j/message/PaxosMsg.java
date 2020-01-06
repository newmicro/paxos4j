package cn.oomkiller.paxos4j.message;

import lombok.*;


@Builder
@Data
public class PaxosMsg extends MessageBody {
    private long proposalId;
    private long nodeId;
    private long instanceId;

    PaxosMsgType msgType;
    long proposalNodeId;
    byte[] value;
    long preAcceptId;
    long preAcceptNodeId;
    long rejectByPromiseId;
    long nowInstanceId;
    long minChosenInstanceId;
    int flag;
    byte[] SystemVariables;
    byte[] MasterVariables;
}
