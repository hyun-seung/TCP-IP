package netty.config;

import lombok.extern.slf4j.Slf4j;
import netty.service.NettyClient;
import netty.service.*;
import netty.service.NettyClientService_v3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NettyClientConfig {

    @Bean
    public NettyClient create(ServerConfig serverConfig) {
        NettyClient nettyClient = new NettyClientService_v3(serverConfig);
        log.info("{} running", nettyClient.getClass().getSimpleName());
        return nettyClient;
    }
}
