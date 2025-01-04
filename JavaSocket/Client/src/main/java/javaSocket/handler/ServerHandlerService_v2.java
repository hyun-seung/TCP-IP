package javaSocket.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class ServerHandlerService_v2 implements ServerHandler {

    // System.in이 닫히면 JVM이 종료되기 전까지 다시 사용 불가 -> 전역으로 선언
    private final BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    private final Socket serverSocket;

    public ServerHandlerService_v2(Socket socket) {
        this.serverSocket = socket;
    }

    @Override
    public void run() {
        try (
            InputStream inputStream = serverSocket.getInputStream();
            OutputStream outputStream = serverSocket.getOutputStream();
        ) {
            String message = "Hello Server";
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
            outputStream.flush();
            log.info("Sent : {}", message);

            byte[] buffer = new byte[1024];
            int readBytes = inputStream.read(buffer);

            if (readBytes != -1) {
                byte[] response = Arrays.copyOf(buffer, readBytes);
                log.info("Received : {}", new String(response, StandardCharsets.UTF_8));
            } else {
                log.warn("No response received from server.");
            }
        } catch (Exception e) {
            log.error("Client error : ", e);
            throw new RuntimeException("Client Error : ", e);
        }
    }
}
