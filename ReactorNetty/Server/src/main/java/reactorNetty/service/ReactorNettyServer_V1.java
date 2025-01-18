package reactorNetty.service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactorNetty.config.ServerConfig;

@Slf4j
public class ReactorNettyServer_V1 implements ReactorNettyServer {

    @Override
    public void initialization() {
        DisposableServer server = TcpServer.create()
                .host(ServerConfig.HOST)
                .port(ServerConfig.PORT)
                .handle((inbound, outbound) ->
                        inbound.receive()
                                .asString()
                                .flatMap(message -> {
                                    log.info("Received : {}", message);

                                    String response = "Echo - " + message;
                                    return outbound.sendString(Mono.just(response))
                                            .then()
                                            .doOnTerminate(() -> log.info("Sent : {}", response));
                                })
                )
                .bindNow();

        server.onDispose()
                .block();
    }
}
