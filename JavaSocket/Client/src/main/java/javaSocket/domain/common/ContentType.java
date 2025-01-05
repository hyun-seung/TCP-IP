package javaSocket.domain.common;

import lombok.Getter;

@Getter
public enum ContentType implements BaseEnum<Byte> {

    JPEG(0x01, ".jpeg", "JPEG"),
    JPG(0x02, ".jpg", "JPG"),
    PNG(0x03, ".png", "PNG"),
    GIF(0x04, ".gif", "GIF"),
    ETC(0x05, ".etc", "기타");

    private final Byte value;
    private final String extension;
    private final String desc;

    ContentType(int value, String extension, String desc) {
        this.value = (byte) (value & 0xFF);
        this.extension = extension;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("0x%02x", value);
    }
}
