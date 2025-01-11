package springIntegration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.CachingClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class TcpClientConfig {

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        String host = ServerConfig.HOST;
        int port = ServerConfig.PORT;

        TcpNioClientConnectionFactory factory = new TcpNioClientConnectionFactory(host, port);
        ByteArrayCrLfSerializer byteArrayCrLfSerializer = new ByteArrayCrLfSerializer();
        factory.setSerializer(byteArrayCrLfSerializer);         // 서버쪽에서 \r\n을 붙여서 전송해줘야함
        factory.setDeserializer(byteArrayCrLfSerializer);
        factory.setSingleUse(true);                             // true인 경우 매번 생성, false인 경우 커넥션 유지(재사용) , Default : false
        factory.setUsingDirectBuffers(true);                    // 직접 메모리 버퍼 사용 , 힙 메모리 X
        factory.setConnectTimeout(ServerConfig.MAX_TIMEOUT);    // seconds
        factory.setSoTimeout(60 * 60 * 1000);                   // milliSeconds, Removing Closed Channel
        factory.setSoKeepAlive(true);
        factory.setSoTcpNoDelay(true);
        log.info("singleUse : {}", factory.isSingleUse());

        CachingClientConnectionFactory cachingFactory = new CachingClientConnectionFactory(factory, 50);
        cachingFactory.setBeanName(String.format("cache_%s:%d", host, port));
        cachingFactory.setHost(host);
        cachingFactory.setPort(port);
        cachingFactory.setSoKeepAlive(true);
        cachingFactory.setSoTcpNoDelay(true);
        log.info("cache-singleUse : {}", cachingFactory.isSingleUse());

        return cachingFactory;
    }

    // 요청 채널
    @Bean(name = "outboundChannel")
    public MessageChannel outboundChannel() {
        return new DirectChannel();
    }

    // 응답 채널
    @Bean(name = "replyChannel")
    public MessageChannel replyChannel() {
        return new DirectChannel();
    }

    // 송수신 Gateway
    @Bean
    public TcpOutboundGateway tcpOutboundGateway(AbstractClientConnectionFactory clientConnectionFactory) {
        TcpOutboundGateway tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(clientConnectionFactory());
        tcpOutboundGateway.setOutputChannel(outboundChannel());
        tcpOutboundGateway.setReplyChannel(replyChannel());
        return tcpOutboundGateway;
    }

    @Bean
    public IntegrationFlow tcpClientFlow(AbstractClientConnectionFactory clientConnectionFactory) {
        return f -> f.channel(outboundChannel())  // 채널 설정
                .handle(tcpOutboundGateway(clientConnectionFactory)); // TcpOutboundGateway 처리
    }

    // 동기
    @Bean
    public MessagingTemplate messagingTemplate() {
        MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(outboundChannel());
        return template;
    }

    @ServiceActivator(inputChannel = "replyChannel")
    public Message<byte[]> handleRequest(Message<byte[]> message) {
        byte[] payload = message.getPayload();
        log.info("Received : {}" , new String(payload, StandardCharsets.UTF_8));
        return MessageBuilder.withPayload(payload).build();
    }
}
