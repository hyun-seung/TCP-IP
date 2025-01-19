package reactorNetty;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

@Slf4j
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        TcpServer.create()
                .host("127.0.0.1")
                .port(12345)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handle((inbound, outbound) ->
                        inbound.receive()
                                .asString()
                                .flatMap(msg -> {
                                    String[] msgs = msg.split("\n");

                                    Mono<Void> responseMono = Mono.empty();
                                    for (String message : msgs) {
                                        String response = "Echo - " + message + "\n";
                                        responseMono = responseMono.then(
                                                outbound.sendString(Mono.just(response)).then()
                                        );
                                    }
                                    return responseMono.then();
                                })
                )
                .doOnBind(server -> log.info("Server started on port {}", 12345))
                .bindNow()
                .onDispose()
                .block();
    }
}
