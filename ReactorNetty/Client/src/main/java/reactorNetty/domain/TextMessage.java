package reactorNetty.domain;

import lombok.Getter;
import reactorNetty.domain.common.CommandId;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
public class TextMessage {
    private final MessageHeader header;
    private final String body;

    public TextMessage(CommandId commandId, String sessionId, String body) {
        int bodyLength = body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0;
        this.header = new MessageHeader(commandId, bodyLength, sessionId);
        this.body = body;
    }

    public TextMessage(MessageHeader header, String body) {
        this.header = header;
        this.body = body;
    }

    public static TextMessage fromBytes(byte[] bytes) {
        byte[] headerBytes = new byte[48];
        System.arraycopy(bytes, 0, headerBytes, 0, 48);
        MessageHeader header = MessageHeader.fromBytes(headerBytes);

        byte[] bodyBytes = new byte[header.getCommandLength()];
        System.arraycopy(bytes, 48, bodyBytes, 0, bodyBytes.length);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        return new TextMessage(header, body);
    }

    public byte[] toBytes() {
        byte[] headerBytes = header.toBytes();
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        buffer.put(headerBytes);
        buffer.put(bodyBytes);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("TextMessage [%s, Body=%s]", header, body);
    }
}
