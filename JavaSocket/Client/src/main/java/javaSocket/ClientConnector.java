package javaSocket;

import javaSocket.config.Constant;
import javaSocket.config.ServerHandlerFactory;
import javaSocket.handler.ServerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientConnector {

    private final ServerHandlerFactory serverHandlerFactory;

    private int attempt = 0;

    public void start() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(Constant.HOST, Constant.PORT), Constant.MAX_TIMEOUT);
            socket.setSoTimeout(Constant.MAX_TIMEOUT);
            log.info("Connected to server at {}:{}", Constant.HOST, Constant.PORT);

            attempt = 0;

            ServerHandler serverHandler = serverHandlerFactory.create(socket);
            serverHandler.run();
        } catch (Exception e) {
//            log.error("Client error : ", e);
            attempt += 1;
            handleReconnect(attempt, e);
        }
    }

    private void handleReconnect(int attempt, Exception e) {
        log.error("Connection failed : {} ( attempt {} / {} )", e.getMessage(), attempt, Constant.MAX_RECONNECT_ATTEMPTS);

        if (attempt >= Constant.MAX_RECONNECT_ATTEMPTS) {
            log.error("Max reconnect attempts reached. Exiting...");
            return;
        }

        log.info("Reconnecting in {} seconds...", Constant.RECONNECT_DELAY_SECONDS);
        try {
            TimeUnit.SECONDS.sleep(Constant.RECONNECT_DELAY_SECONDS);
            start();
        } catch (Exception ee) {
            log.error("Reconnect delay interrupted : ", ee);
            Thread.currentThread().interrupt();
        }
    }
}
