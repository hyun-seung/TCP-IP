package javaSocket.config;

import javaSocket.handler.ClientHandler;
import javaSocket.handler.ClientHandlerService_v1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.Socket;

@Slf4j
@Component
public class ClientHandlerFactory {

    public ClientHandler create(Socket socket) {
        ClientHandler clientHandler = new ClientHandlerService_v1(socket);
        log.info("{} RUN", clientHandler.getClass());
        return clientHandler;
    }
}
