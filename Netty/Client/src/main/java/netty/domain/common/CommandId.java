package netty.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandId implements BaseEnum<Integer> {
    REQ(0x00001001, "요청"),
    RES(0x00001002, "응답");

    private final Integer value;
    private final String desc;

    @Override
    public String toString() {
        return String.format("0x%08X", value);
    }
}
