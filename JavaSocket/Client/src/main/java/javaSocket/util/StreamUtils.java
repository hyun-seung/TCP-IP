package javaSocket.util;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static byte[] readFully(InputStream inputStream, int length) throws IOException {
        byte[] buffer = new byte[length];
        int bytesRead = 0;

        while (bytesRead < length) {
            int result = inputStream.read(buffer, bytesRead, length - bytesRead);
            if (result == -1) {
                throw new IOException("Stream closed prematurely while reading data");
            }
            bytesRead += result;
            System.out.println(length);
            System.out.println(bytesRead);
            System.out.println();
        }
        return buffer;
    }
}
