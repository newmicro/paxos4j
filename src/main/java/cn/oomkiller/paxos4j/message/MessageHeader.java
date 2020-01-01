package cn.oomkiller.paxos4j.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageHeader {
    private int version = 1;
    private int opCode;
    private long streamId;

    public MessageHeader(int opCode, long streamId) {
        this.opCode = opCode;
        this.streamId = streamId;
    }
}
