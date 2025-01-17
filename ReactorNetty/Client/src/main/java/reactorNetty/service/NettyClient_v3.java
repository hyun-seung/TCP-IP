 package reactorNetty.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactorNetty.config.ServerConfig;
import reactorNetty.domain.MessageHeader;
import reactorNetty.domain.TextMessage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

 @Slf4j
 public class NettyClient_v3 implements NettyClient<TextMessage> {

     private final ServerConfig serverConfig;

     private Connection connection;

     public NettyClient_v3(ServerConfig serverConfig) {
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
                                 .flatMap(byteBuf -> {
                                     int count;
                                     while ((count = byteBuf.readableBytes()) > 0) {
                                         log.info("Count : {}", count);
                                         getTextMessage(byteBuf);
                                     }
                                     return Mono.empty();
                                 })
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

     private TextMessage getTextMessage(ByteBuf byteBuf) {
         byte[] headerBytes = new byte[48];
         byteBuf.readBytes(headerBytes);
         MessageHeader header = MessageHeader.fromBytes(headerBytes);

         int bodyLength = header.getCommandLength();
         byte[] bodyBytes = new byte[bodyLength];
         byteBuf.readBytes(bodyBytes);
         String body = new String(bodyBytes, StandardCharsets.UTF_8);

         TextMessage textMessage = new TextMessage(header, body);

         log.info("Received : {}", textMessage);
         return textMessage;
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
     public void sendMessage(TextMessage textMessage) {
         if (isConnected()) {
             connection.outbound()
                     .sendObject(Mono.just(Unpooled.wrappedBuffer(textMessage.toBytes())))
                     .then()
                     .doOnTerminate(() -> log.info("Sent : {}", textMessage))
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
                         sendMessage(textMessage);
                     })
                     .doOnError(error -> log.error("Error #2 : {}", error.getMessage()))
                     .subscribe();
         }
     }

     private boolean isConnected() {
         return Objects.nonNull(connection) && !connection.isDisposed();
     }
 }
