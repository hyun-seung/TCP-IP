package springIntegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springIntegration.service.TcpClientService;

import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(TcpClientService tcpClientService) {
        return args -> {
            try {
                String message = "Hello, Server!";
                byte[] bytes = tcpClientService.sendAndReceive(message.getBytes(StandardCharsets.UTF_8));

                log.info("서버 응답 : " + new String(bytes, StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("메시지 송수신 실패");
            }
        };
    }

}
