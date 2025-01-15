package server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import server.service.NettyServer;
import server.service.NettyServer_v1;

@Slf4j
@Configuration
public class NettyServerConfig {

    @Bean
    public NettyServer create() {
        NettyServer nettyServer = new NettyServer_v1();
        log.info("{} running", nettyServer.getClass().getSimpleName());
        return nettyServer;
    }
}
