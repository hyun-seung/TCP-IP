package javaSocket.handler;

import javaSocket.config.Constant;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientHandlerService_v2 implements ClientHandler {

    private final Socket clientSocket;

    public ClientHandlerService_v2(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            while (true) {
                int availableBytes = inputStream.available();
                if (availableBytes > 0) {
                    log.info("Readable bytes : {}", availableBytes);

                    byte[] buffer = new byte[availableBytes];
                    inputStream.read(buffer);
                    String receivedData = new String(buffer);
                    log.info("Received : {}", receivedData);

                    String responseData = "Echo - " + receivedData;
                    outputStream.write(responseData.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();

                    log.info("Sent : {}", responseData);
                }
            }
        } catch (Exception e) {
            if (Constant.CONNECTION_RESET.equalsIgnoreCase(e.getMessage())) {
                log.warn("Connection reset by client : {}", clientSocket.getInetAddress());
            } else {
                log.error("Error handling Client : ",  e);
            }
        } finally {
            try {
                clientSocket.close();
                log.info("Client disconnected");
            } catch (Exception e) {
                log.error("Error closing client socket : ", e);
            }
        }
    }
}
