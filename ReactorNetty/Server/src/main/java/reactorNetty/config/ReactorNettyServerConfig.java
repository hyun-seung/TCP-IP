package reactorNetty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactorNetty.service.ReactorNettyServer;
import reactorNetty.service.ReactorNettyServer_V1;
import reactorNetty.service.ReactorNettyServer_V2;

@Slf4j
@Configuration
public class ReactorNettyServerConfig {

    @Bean
    public ReactorNettyServer create() {
        ReactorNettyServer server = new ReactorNettyServer_V2();
        log.info("{} running", server.getClass().getSimpleName());
        return server;
    }
}
