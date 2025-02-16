package javaSocket.config;

import javaSocket.handler.ServerHandler;
import javaSocket.handler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.Socket;

@Slf4j
@Component
public class ServerHandlerFactory {

    public ServerHandler create(Socket socket) {
        ServerHandler serverHandler = new ServerHandlerService_v4(socket);
        log.info("{} RUN", serverHandler.getClass());
        return serverHandler;
    }
}
