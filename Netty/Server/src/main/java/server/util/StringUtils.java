package server.util;

public class StringUtils {

    public static String fixedLength(String input, int length) {
        if (input.length() > length) {
            return input.substring(0, length);
        }
        return String.format("%1$-" + length + "s", input);
    }
}
