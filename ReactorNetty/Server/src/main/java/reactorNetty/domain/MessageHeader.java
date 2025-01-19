package reactorNetty.domain;

import lombok.Getter;
import reactorNetty.domain.common.BaseEnum;
import reactorNetty.domain.common.CommandId;
import reactorNetty.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
public class MessageHeader {

    private static final int SESSION_ID_BYTES_LENGTH = 40;

    private final CommandId commandId;  // 메시지 타입 구분, 4바이트
    private final int commandLength;    // 메시지 body의 길이, 4바이트
    private final String sessionId;     // 메시지 고유의 값, 40바이트

    public MessageHeader(CommandId commandId, int commandLength, String sessionId) {
        this.commandId = commandId;
        this.commandLength = commandLength;
        this.sessionId = StringUtils.fixedLength(sessionId, SESSION_ID_BYTES_LENGTH);
    }

    public static MessageHeader fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        CommandId commandId = BaseEnum.fromValue(CommandId.class, buffer.getInt());
        int commandLength = buffer.getInt();
        byte[] sessionIdBytes = new byte[SESSION_ID_BYTES_LENGTH];
        buffer.get(sessionIdBytes);
        String sessionId = new String(sessionIdBytes, StandardCharsets.UTF_8).trim();

        return new MessageHeader(commandId, commandLength, sessionId);
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(48);
        buffer.putInt(commandId.getValue());
        buffer.putInt(commandLength);
        buffer.put(sessionId.getBytes(StandardCharsets.UTF_8));
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("Header [CommandId=%s, CommandLength=%d, SessionId=%s]",
                commandId, commandLength, sessionId);
    }
}
