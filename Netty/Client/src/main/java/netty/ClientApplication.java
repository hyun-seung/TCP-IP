package netty;

import lombok.extern.slf4j.Slf4j;
import netty.domain.TextMessage;
import netty.domain.common.CommandId;
import netty.service.NettyClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
            try {
//                nettyClient.connect();

                String msg = "Hello Server!";
                TextMessage textMessage = new TextMessage(CommandId.REQ, "20251852-1-msg", msg);
                nettyClient.sendMessage(textMessage);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("메시지 송수신 실패");
            }
        };
    }
}
