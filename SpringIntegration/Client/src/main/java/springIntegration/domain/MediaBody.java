package springIntegration.domain;

import lombok.Getter;
import springIntegration.domain.common.BaseEnum;
import springIntegration.domain.common.ContentType;
import springIntegration.util.ByteUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MediaBody {

    private final int mediaCount;
    private final List<MediaInfo> mediaInfos;

    public MediaBody(List<MediaInfo> mediaInfos) {
        this.mediaCount = mediaInfos.size();
        this.mediaInfos = mediaInfos;
    }

    public static MediaBody fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int mediaCount = buffer.getInt();
        List<MediaInfo> mediaInfos = new ArrayList<>();
        for (int i=0; i<mediaCount; i++) {
            int contentId = buffer.getShort();
            ContentType contentType = BaseEnum.fromValue(ContentType.class, buffer.get());
            int contentWidth = buffer.getShort();
            int contentHeight = buffer.getShort();
            int contentLength = buffer.getInt();
            String contentName = ByteUtils.getString(buffer, 128);

            byte[] contents = new byte[contentLength];
            buffer.get(contents);

            mediaInfos.add(new MediaInfo(contentId, contentType, contentWidth, contentHeight, contentName, contents));
        }

        return new MediaBody(mediaInfos);
    }

    public byte[] toBytes() {
        int mediaInfosLength = mediaInfos.stream().mapToInt(m -> m.toBytes().length).sum();

        ByteBuffer buffer = ByteBuffer.allocate(4 + mediaInfosLength);
        buffer.putInt(mediaCount);
        mediaInfos.forEach(m -> buffer.put(m.toBytes()));

        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("MediaBody [mediaCount=%d, mediaInfos=%s]",
                mediaCount, mediaInfos);
    }
}
