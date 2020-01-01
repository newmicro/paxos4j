package cn.oomkiller.paxos4j.message;

import java.util.function.Predicate;

public enum RequestType {
  PAXOS(1, PaxosMsg.class);

  private int opCode;
  private Class<? extends MessageBody> messageBodyClazz;

  RequestType(int opCode, Class<? extends MessageBody> messageBodyClazz) {
    this.opCode = opCode;
    this.messageBodyClazz = messageBodyClazz;
  }

  public int getOpCode() {
    return opCode;
  }

  public Class<? extends MessageBody> getMessageBodyClazz() {
    return messageBodyClazz;
  }

  public static RequestType fromOpCode(int type) {
    return getOperationType(requestType -> requestType.opCode == type);
  }

  public static RequestType fromOperation(MessageBody operation) {
    return getOperationType(requestType -> requestType.messageBodyClazz == operation.getClass());
  }

  private static RequestType getOperationType(Predicate<RequestType> predicate) {
    RequestType[] values = values();
    for (RequestType operationType : values) {
      if (predicate.test(operationType)) {
        return operationType;
      }
    }

    throw new AssertionError("no found type");
  }
}
