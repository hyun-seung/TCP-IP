package reactorNetty.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactorNetty.config.ServerConfig;
import reactorNetty.domain.MessageHeader;
import reactorNetty.domain.TextMessage;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
public class ReactorNettyServer_V3 implements ReactorNettyServer {

    @Override
    public void initialization() {
        DisposableServer server = TcpServer.create()
                .host(ServerConfig.HOST)
                .port(ServerConfig.PORT)
                .handle((inbound, outbound) ->
                        inbound.receive()
                                .flatMap(byteBuf -> {
                                    int count;
                                    while ((count = byteBuf.readableBytes()) > 0) {
                                        log.info("Count : {}", count);
                                        TextMessage textMessage = getTextMessage(byteBuf);

                                        TextMessage response = new TextMessage(
                                                textMessage.getHeader().getCommandId(), textMessage.getHeader().getSessionId(),
                                                "Hello, Client");

                                        outbound.sendObject(Mono.just(Unpooled.wrappedBuffer(response.toBytes())))
                                                .then()
                                                .doOnTerminate(() -> log.info("Sent : {}", response))
                                                .subscribe();
                                    }
                                    return Mono.empty();
                                })
                )
                .bindNow();

        server.onDispose()
                .block();
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
}
