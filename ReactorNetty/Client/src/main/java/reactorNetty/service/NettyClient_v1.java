package reactorNetty.service;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactorNetty.config.ServerConfig;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class NettyClient_v1 implements NettyClient<String> {

    private final ServerConfig serverConfig;

    private Connection connection;

    public NettyClient_v1(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        initializeConnection();
    }

    private void initializeConnection() {
        if (isConnected()) {
            log.warn("Already connection is successful");
            return;
        }

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

                        serverConfig.setReconnectingFlagToFalse();

                        // 항상 열려있는 수신 스트림
                        conn.inbound()
                                .receive()
                                .asString()
                                .doOnNext(response -> log.info("Received : {}", response))
                                .subscribe();

                        conn.onDispose(() -> {
                            log.warn("Connection closed. Scheduling reconnect...");
                            scheduleReconnect();
                        });
                    })
                    .doOnError(error -> {
                        log.error("Failed connect to server #1 :: {}:{} || {}", ServerConfig.HOST, ServerConfig.PORT, error.getMessage());
                        scheduleReconnect();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed connect to server #2 :: {}:{} || {}", ServerConfig.HOST, ServerConfig.PORT, e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (serverConfig.checkStartReconnecting()) {
            Mono.defer(() -> {
                        log.info("Reconnecting to server ...");
                        initializeConnection();
                        return Mono.empty();
                    })
                    .delaySubscription(Duration.ofSeconds(5))
                    .repeat(() -> !isConnected())
                    .doOnTerminate(() -> log.info("Reconnection attempts stopped. Connection is successful."))
                    .subscribe();
        } else {
            log.warn("Can not reconnect to server. Already Reconnecting to server.");
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
                    .doOnError(error -> log.error("Error #1 : {}", error.getMessage()))
                    .subscribe();
        } else {
            log.warn("Connection not available. Waiting for connection to send message...");
            Mono.defer(() ->
                            Mono.create(sink -> {
                                Mono.delay(Duration.ofMillis(100))
                                        .repeat(() -> !isConnected())
                                        .doOnTerminate(() -> {
                                            if (isConnected()) {
                                                sink.success("Connection established!");
                                            } else {
                                                sink.error(new IllegalStateException("Failed to establish connection."));
                                            }
                                        })
                                        .subscribe();
                            }))
                    .doOnNext(c -> {
                        sendMessage(msg);
                    })
                    .doOnError(error -> log.error("Error #2 : {}", error.getMessage()))
                    .subscribe();
        }
    }

    private boolean isConnected() {
        return Objects.nonNull(connection) && !connection.isDisposed();
    }
}
