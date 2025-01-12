package netty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class ServerConfig {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 12345;

    public static final int MAX_TIMEOUT = 3; // 3s

    private static final List<String> HOSTS = Arrays.asList(
            "naver.com", "localhost"
    );

    public static boolean isFailOverEnabled = true;

    // 현재 서버 정보 확인
    private static int currentHostIndex = -1;

    // 한 사이클(HOSTS) 당 1 증가
    private static int currentRetryCount = 0;

    // 한 사이클 당 대기 시간 (단위 : seconds)
    private static final int RETRY_TIME_1 = 1;
    private static final int RETRY_TIME_2 = 5;
    private static final int RETRY_TIME_3 = 10;
    private static final int RETRY_TIME_4 = 60;

    public String getNextHost() {
        // FailOver 기능 사용 X
        if (!isFailOverEnabled) {
            return HOST;
        }

        currentHostIndex += 1;
        if (currentHostIndex >= HOSTS.size()) {
            log.warn("CurrentHostIndex Max. Reset to zero. CurrentHostIndex :{} -> 0", currentHostIndex);
            currentHostIndex = 0;
            currentRetryCount += 1;
            log.warn("Increase currentRetryCount {} -> {}", currentRetryCount - 1, currentRetryCount);
        }
        return HOSTS.get(currentHostIndex);
    }

    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    public void clearCurrentRetryCount() {
        currentRetryCount = 0;
    }

    public int getRetryTime() {
        if (currentRetryCount == 0) {
            return RETRY_TIME_1;
        } else if (currentRetryCount == 1) {
            return RETRY_TIME_2;
        } else if (currentRetryCount == 2) {
            return RETRY_TIME_3;
        } else {
            return RETRY_TIME_4;
        }
    }
}
