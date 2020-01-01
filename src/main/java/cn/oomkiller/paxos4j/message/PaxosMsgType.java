package cn.oomkiller.paxos4j.message;

import lombok.Getter;

@Getter
public enum PaxosMsgType {
    Prepare(1),
    PrepareReply(2),
    Accept(3),
    AcceptReply(4),
    AskForLearn(5),
    SendLearnValue(6),
    ProposerSendSuccess(7),
    SendNewValue(8),
    SendNowInstanceId(9),
    ConfirmAskForLearn(10),
    SendLearnValueAck(11),
    AskForCheckpoint(12),
    OnAskForCheckpoint(13);

    private int type;
    PaxosMsgType(int type) {
        this.type = type;
    }
}
