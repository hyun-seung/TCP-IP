package springIntegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springIntegration.domain.TextMessage;
import springIntegration.domain.common.CommandId;
import springIntegration.service.TcpClientService;

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
                TextMessage textMessage = new TextMessage(CommandId.REQ, "TestMessage-20240112_01", "Hello Server");
                TextMessage responseTextMessage = tcpClientService.sendAndReceive(textMessage);

                log.info("서버 응답 : {}", responseTextMessage);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("메시지 송수신 실패");
            }
        };
    }

}
