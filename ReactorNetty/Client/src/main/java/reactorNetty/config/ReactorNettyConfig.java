package reactorNetty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactorNetty.service.NettyClient;
import reactorNetty.service.NettyClient_v1;
import reactorNetty.service.NettyClient_v2;
import reactorNetty.service.NettyClient_v3;

@Slf4j
@Configuration
public class ReactorNettyConfig {

    @Bean
    public NettyClient create(ServerConfig serverConfig) {
        NettyClient nettyClient = new NettyClient_v3(serverConfig);
        log.info("{} running", nettyClient.getClass().getSimpleName());
        return nettyClient;
    }
}
