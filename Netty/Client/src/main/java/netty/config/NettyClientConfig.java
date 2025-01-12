package netty.config;

import lombok.extern.slf4j.Slf4j;
import netty.service.NettyClient;
import netty.service.NettyClientService_v1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NettyClientConfig {

    @Bean
    public NettyClient create() {
        NettyClient nettyClient = new NettyClientService_v1();
        log.info("{} running", nettyClient.getClass().getSimpleName());
        return nettyClient;
    }
}
