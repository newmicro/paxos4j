package cn.oomkiller.paxos4j.message;

import cn.oomkiller.paxos4j.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
public class Message<T extends MessageBody> {
  private MessageHeader header;
  private T body;

  public Message(Long streamId, T body) {
    this.header = new MessageHeader(RequestType.fromOperation(body).getOpCode(), streamId);
    this.body = body;
  }

  public T getBody() {
    return body;
  }

  private Class<T> getMessageBodyDecodeClass() {
    return (Class<T>) RequestType.fromOpCode(header.getOpCode()).getMessageBodyClazz();
  }

  public void encode(ByteBuf buf) {
    buf.writeInt(header.getVersion());
    buf.writeInt(header.getOpCode());
    buf.writeLong(header.getStreamId());
    buf.writeBytes(JsonUtil.toJson(body).getBytes());
  }

  public void decode(ByteBuf buf) {
    int version = buf.readInt();
    int opCode = buf.readInt();
    long streamId = buf.readLong();
    this.header = new MessageHeader(version, opCode, streamId);

    Class<T> clazz = getMessageBodyDecodeClass();
    this.body = JsonUtil.fromJson(buf.toString(StandardCharsets.UTF_8), clazz);
  }
}
