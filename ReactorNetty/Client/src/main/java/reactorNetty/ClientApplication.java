package reactorNetty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactorNetty.service.NettyClient;

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
            nettyClient.sendMessage(msg);
        };
    }
}
