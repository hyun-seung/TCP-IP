package reactorNetty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactorNetty.domain.TextMessage;
import reactorNetty.domain.common.CommandId;
import reactorNetty.service.NettyClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(NettyClient nettyClient) {
        return args -> {
            String msg = "Hello Server!";
            TextMessage textMessage = new TextMessage(CommandId.REQ, "202501171744-1-msg", msg);
            nettyClient.sendMessage(textMessage);
        };
    }
}
