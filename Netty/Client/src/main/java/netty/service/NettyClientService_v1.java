package netty.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netty.config.ServerConfig;
import netty.handler.ClientHandler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NettyClientService_v1 implements NettyClient<String> {

    private final ServerConfig serverConfig;

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(8);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Channel channel;    // 데이터 송수신, 연결 상태 관리

    @Override
    public CompletableFuture<Void> connect() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addLast(new StringDecoder());
                        channelPipeline.addLast(new StringEncoder());
                        channelPipeline.addLast(new ClientHandler());
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ServerConfig.MAX_TIMEOUT * 1000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_LINGER, 0)
                .option(ChannelOption.SO_REUSEADDR, true);

        CompletableFuture<Void> result = new CompletableFuture<>();
        connectToServer(bootstrap)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        result.completeExceptionally(ex);
                    } else {
                        result.complete(null);
                    }
                });

        return result;
    }

    @Override
    public CompletableFuture<Void> connectToServer(Bootstrap bootstrap) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        String host = serverConfig.getNextHost();
        int port = ServerConfig.PORT;
        log.info("Attempting to connect to server : {}:{}", host, port);

        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            channel = future.channel();
                            log.info("Successfully connected to server : {}:{}", host, port);
                            serverConfig.clearCurrentRetryCount();
                            result.complete(null);
                        } else {
                            log.warn("Failed connect to server : {}:{}", host, port);
                            log.warn("Now RetryCount : {} , Retry Delay Time : {}s",
                                    serverConfig.getCurrentRetryCount(), serverConfig.getRetryTime());
                            scheduler.schedule(() ->
                                    connectToServer(bootstrap)
                                            .whenComplete((res, ex) -> {
                                                if (ex != null) {
                                                    result.completeExceptionally(ex);
                                                } else {
                                                    result.complete(null);
                                                }
                                            }), serverConfig.getRetryTime(), TimeUnit.SECONDS);
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    @Override
    public CompletableFuture<Void> sendMessage(String msg) {
        if (Objects.isNull(channel) || !channel.isActive()) {
            log.warn("Channel is not active, Reconnecting...");
            return connect().thenCompose(res -> sendMessage(msg));  // 재연결 후 메시지 전송
        }

        CompletableFuture<Void> result = new CompletableFuture<>();

        String newMsg = msg + "\n";
        ChannelFuture future = channel.writeAndFlush(newMsg);
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                log.info("Sent : {}", msg);
                result.complete(null);
            } else {
                result.completeExceptionally(f.cause());
            }
        });

        return result;
    }

    @Override
    public CompletableFuture<Void> close() {
        if (Objects.isNull(channel) || !channel.isActive()) {
            return CompletableFuture.completedFuture(null);     // 이미 연결이 종료된 경우
        }

        CompletableFuture<Void> result = new CompletableFuture<>();

        ChannelFuture closeFuture = channel.close();
        closeFuture.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                log.info("Connection closed successfully");
            } else {
                log.error("Failed to close connection : ", f.cause());
            }
            shutdownEventLoopGroup(result);
        });

        return result;
    }

    private void shutdownEventLoopGroup(CompletableFuture<Void> result) {
        try {
            eventLoopGroup.shutdownGracefully().sync();
            log.info("EventLoopGroup shutdown completed");
            result.complete(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("EventLoopGroup shutdown interrupted.");
            result.completeExceptionally(e);
        }
    }
}
