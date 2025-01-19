package reactorNetty.service;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactorNetty.config.ServerConfig;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ReactorNettyServer_V2 implements ReactorNettyServer {

    @Override
    public void initialization() {
        DisposableServer server = TcpServer.create()
                .host(ServerConfig.HOST)
                .port(ServerConfig.PORT)
                .handle((inbound, outbound) ->
                        inbound.receive()
                                .asByteArray()
                                .flatMap(bytes -> {
                                    String msg = new String(bytes, StandardCharsets.UTF_8);
                                    log.info("Received : {}", msg);

                                    String response = "Echo - " + msg;
                                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                                    return outbound.sendByteArray(Mono.just(responseBytes))
                                            .then()
                                            .doOnTerminate(() -> log.info("Sent : {}", response));
                                })
                )
                .bindNow();

        server.onDispose()
                .block();
    }
}
