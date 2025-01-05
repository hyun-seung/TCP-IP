package javaSocket.handler;

import javaSocket.config.Constant;
import javaSocket.domain.MessageHeader;
import javaSocket.domain.TextMessage;
import javaSocket.domain.common.CommandId;
import javaSocket.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientHandlerService_v3 implements ClientHandler {

    private final Socket clientSocket;

    public ClientHandlerService_v3(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            while (true) {
                byte[] headerBytes = getReadNBytes(inputStream, 48);
                MessageHeader header = MessageHeader.fromBytes(headerBytes);

                byte[] bodyBytes = getReadNBytes(inputStream, header.getCommandLength());
                String body = new String(bodyBytes, StandardCharsets.UTF_8);

                TextMessage textMessage = new TextMessage(header, body);
                log.info("Received : {}", textMessage);

                TextMessage responseMessage = new TextMessage(CommandId.RES, header.getSessionId(), "Hello Client");
                byte[] responseMessageBytes = responseMessage.toBytes();

                outputStream.write(responseMessageBytes);
                outputStream.flush();

                log.info("Sent : {}", responseMessage);
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

    private static byte[] getReadNBytes(InputStream inputStream, int length) throws IOException {
        // java 11 이상
        return inputStream.readNBytes(length);

        // java 11 이하
//        return StreamUtils.readFully(inputStream, length);
    }
}
