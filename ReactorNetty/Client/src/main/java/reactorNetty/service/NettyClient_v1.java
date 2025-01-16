package reactorNetty.service;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactorNetty.config.ServerConfig;

import java.util.Objects;

@Slf4j
public class NettyClient_v1 implements NettyClient<String> {

    private Connection connection;

    public NettyClient_v1() {
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            TcpClient.create()
                    .host(ServerConfig.HOST)
                    .port(ServerConfig.PORT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_LINGER, 0)
                    .connect()
                    .doOnSuccess(conn -> {
                        this.connection = conn;
                        log.info("Connected to server :: {}:{}", ServerConfig.HOST, ServerConfig.PORT);

                        // 항상 열려있는 수신 스트림
                        conn.inbound()
                                .receive()
                                .asString()
                                .doOnNext(response -> log.info("Received : {}", response))
                                .subscribe();

                        conn.onDispose(() -> {
                            log.warn("Connection closed. Scheduling reconnect...");
                        });
                    })
                    .doOnError(error -> {
                        log.error("Failed connect to server #1 :: {}:{} || {}", ServerConfig.HOST, ServerConfig.PORT, error.getMessage());
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed connect to server #2 :: {}:{} || {}", ServerConfig.HOST, ServerConfig.PORT, e.getMessage());
        }
    }

    @Override
    public void sendMessage(String msg) {
        if (isConnected()) {
            connection.outbound()
                    .sendString(Mono.just(msg))
                    .then()
                    .doOnTerminate(() -> log.info("Sent : {}", msg))
                    .then()
                    .doOnError(error -> log.error("Error : {}", error.getMessage()))
                    .subscribe();
        }
    }

    private boolean isConnected() {
        return Objects.nonNull(connection) && !connection.isDisposed();
    }
}
