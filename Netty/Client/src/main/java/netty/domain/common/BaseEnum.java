package netty.domain.common;

import java.util.Arrays;

public interface BaseEnum<T> {

    T getValue();

    static <E extends Enum<E> & BaseEnum<T>, T> E fromValue(Class<E> enumClass, T value) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(value))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Unknown value '%s' for enum %s", value, enumClass.getSimpleName())
                ));
    }
}
