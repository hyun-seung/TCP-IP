package netty.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteUtils {

    public static String getString(ByteBuffer buffer, int size) {
        byte[] tempBytes = new byte[size];
        buffer.get(tempBytes);
        return new String(tempBytes, StandardCharsets.UTF_8).trim();
    }
}
