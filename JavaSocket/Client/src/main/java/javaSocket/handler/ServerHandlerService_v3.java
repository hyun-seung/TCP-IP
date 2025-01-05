package javaSocket.handler;

import javaSocket.domain.MessageHeader;
import javaSocket.domain.TextMessage;
import javaSocket.domain.common.CommandId;
import javaSocket.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class ServerHandlerService_v3 implements ServerHandler {

    private final Socket serverSocket;

    public ServerHandlerService_v3(Socket socket) {
        this.serverSocket = socket;
    }

    @Override
    public void run() {
        try (
            InputStream inputStream = serverSocket.getInputStream();
            OutputStream outputStream = serverSocket.getOutputStream();
        ) {
            TextMessage textMessage = new TextMessage(CommandId.REQ, "TestMessage-20240105_01", "Hello Server");
            byte[] textMessageBytes = textMessage.toBytes();

            outputStream.write(textMessageBytes);
            outputStream.flush();
            log.info("Sent : {}", textMessage);

            byte[] headerBytes = getReadNBytes(inputStream, 48);
            MessageHeader header = MessageHeader.fromBytes(headerBytes);

            byte[] bodyBytes = getReadNBytes(inputStream, header.getCommandLength());
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            TextMessage responseMessage = new TextMessage(header, body);
            log.info("Received : {}", responseMessage);
        } catch (Exception e) {
            log.error("Client error : ", e);
            throw new RuntimeException("Client Error : ", e);
        }
    }

    private static byte[] getReadNBytes(InputStream inputStream, int length) throws IOException {
        // java 11 이상
//        return inputStream.readNBytes(length);

        // java 11 이하
        return StreamUtils.readFully(inputStream, length);
    }
}
