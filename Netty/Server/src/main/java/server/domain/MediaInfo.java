package server.domain;

import lombok.Getter;
import server.domain.common.BaseEnum;
import server.domain.common.ContentType;
import server.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
public class MediaInfo {

    private final int contentId;            // MEDIA ID (MEDIA 고유 식별자), 2바이트
    private final ContentType contentType;  // 1바이트
    private final int contentWidth;         // Contents 가로 (단위: 픽셀), 2바이트
    private final int contentHeight;        // Contents 세로 (단위: 픽셀), 2바이트
    private final int contentLength;        // Contents 길이, 4바이트
    private final String contentName;       // Image File Name, 128바이트
    private final byte[] contents;          // IMAGE BINARY

    public MediaInfo(int contentId, ContentType contentType,
                     int contentWidth, int contentHeight, String contentName, byte[] contents) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.contentLength = contents.length;
        this.contentName = StringUtils.fixedLength(contentName, 128);
        this.contents = contents;
    }

    public static MediaInfo fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int contentId = buffer.getShort();
        ContentType contentType = BaseEnum.fromValue(ContentType.class, buffer.get());
        int contentWidth = buffer.getShort();
        int contentHeight = buffer.getShort();
        int contentLength = buffer.getInt();
        byte[] contentNameBytes = new byte[128];
        buffer.get(contentNameBytes);
        String contentName = new String(contentNameBytes, StandardCharsets.UTF_8).trim();

        byte[] contents = new byte[contentLength];
        buffer.get(contents);

        return new MediaInfo(contentId, contentType, contentWidth, contentHeight, contentName, contents);
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(139 + contents.length);

        buffer.putShort((short) (contentId & 0xFFFF));
        buffer.put((byte) (contentType.getValue() & 0xFF));
        buffer.putShort((short) (contentWidth & 0xFFFF));
        buffer.putShort((short) (contentHeight & 0xFFFF));
        buffer.putInt(contentLength);
        buffer.put(contentName.getBytes(StandardCharsets.UTF_8));
        buffer.put(contents);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("MediaInfo [ConetntId=%d, ContentType=%s, ContentWidth=%d, ContentHeight=%d, ContentLength=%d, ContentName=%s]",
                contentId, contentType, contentWidth, contentHeight, contentLength, contentName);
    }
}
