package cn.oomkiller.paxos4j.message;

import java.io.Serializable;
import lombok.Builder;

@Builder
public class AcceptorStateData implements Serializable {
    long instanceId ;
    long promiseId;
    long promiseNodeId;
    long acceptedId;
    long acceptedNodeId;
    byte[] acceptedValue;
}
