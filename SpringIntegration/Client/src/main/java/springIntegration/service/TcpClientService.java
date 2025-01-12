package springIntegration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import springIntegration.domain.TextMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpClientService {

    private final MessagingTemplate messagingTemplate;

    public TextMessage sendAndReceive(TextMessage textMessage) {
        // 요청 메시지 생성
        Message<byte[]> requestMessage = MessageBuilder.withPayload(textMessage.toBytes()).build();

        // 메시지 송수신
        Message<?> replyMessage = messagingTemplate.sendAndReceive(requestMessage);
        if (replyMessage != null) {
            return (TextMessage) replyMessage.getPayload();
        } else {
            throw new RuntimeException("응답을 받지 못했습니다");
        }
    }
}
