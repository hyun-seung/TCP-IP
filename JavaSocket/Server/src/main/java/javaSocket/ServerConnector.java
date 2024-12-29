package javaSocket;

import javaSocket.config.ClientHandlerFactory;
import javaSocket.config.Constant;
import javaSocket.handler.ClientHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerConnector {

    private final ClientHandlerFactory clientHandlerFactory;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Constant.PORT)) {
           log.info("\nJavaSocket Server started on port {}", Constant.PORT);

           while (true) {
               Socket clientSocket = serverSocket.accept();
               log.info("Client connected : {}:{}", clientSocket.getInetAddress(), clientSocket.getPort());

               ClientHandler clientHandler = clientHandlerFactory.create(clientSocket);
               Thread thread = new Thread(clientHandler);
               thread.start();
           }
        } catch (Exception e) {
            log.error("Server error : ", e);
        }
    }
}
