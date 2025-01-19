package spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
public class TcpServerConfig {

    @Bean
    public AbstractServerConnectionFactory tcpNioServerConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(ServerConfig.PORT);
        factory.setUsingDirectBuffers(true);
        factory.setBacklog(100);
        return new TcpNioServerConnectionFactory(ServerConfig.PORT);
    }

    @Bean
    public MessageChannel requestChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway tcpInboundGateway() {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(tcpNioServerConnectionFactory());
        gateway.setRequestChannel(requestChannel());
        gateway.setRequestTimeout(5000);
        return gateway;
    }

    @ServiceActivator(inputChannel = "requestChannel")
    public String handle(String message) {
        log.info("Received : {}", message);
        return "Echo - " + message;
    }
}
