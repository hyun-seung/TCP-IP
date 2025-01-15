package netty.domain;

import lombok.Getter;
import netty.domain.common.CommandId;

import java.nio.ByteBuffer;

@Getter
public class MediaMessage {
    private final MessageHeader header;
    private final MediaBody body;

    public MediaMessage(CommandId commandId, String sessionid, MediaBody body) {
        int bodyLength = body != null ? body.toBytes().length : 0;
        this.header = new MessageHeader(commandId, bodyLength, sessionid);
        this.body = body;
    }

    public byte[] toBytes() {
        byte[] headerBytes = header.toBytes();
        byte[] bodyBytes = body.toBytes();

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        buffer.put(headerBytes);
        buffer.put(bodyBytes);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("MediaMessage [%s, %s]", header, body);
    }
}
