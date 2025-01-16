package reactorNetty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Configuration
public class ServerConfig {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 12345;

    // 재연결 진행 FLAG
    private AtomicBoolean isReconnecting = new AtomicBoolean(false);

    public boolean checkStartReconnecting() {
        if (isReconnecting.get()) {
            // 재연결 불가
            return false;
        } else {
            isReconnecting.set(true);
            return true;
        }
    }

    public void setReconnectingFlagToFalse() {
        isReconnecting.set(false);
    }
}
