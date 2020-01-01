package cn.oomkiller.paxos4j.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AcceptorStateData implements Serializable {
    long instanceId ;
    long promiseId;
    long promiseNodeId;
    long acceptedId;
    long acceptedNodeId;
    byte[] acceptedValue;
}
