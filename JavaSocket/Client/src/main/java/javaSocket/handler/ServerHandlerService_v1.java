package javaSocket.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class ServerHandlerService_v1 implements ServerHandler {

    // System.in이 닫히면 JVM이 종료되기 전까지 다시 사용 불가 -> 전역으로 선언
    private final BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    private final Socket serverSocket;

    private static final String BYE = "bye";

    public ServerHandlerService_v1(Socket socket) {
        this.serverSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream()))
        ) {
            String input;
            while (true) {
                System.out.print("Enter message (type 'bye' to exit) : ");
                input = userInput.readLine();

                if (Objects.isNull(input) || input.trim().isEmpty()) {
                    log.info("Empty message. Please type something");
                    continue;
                }

                out.println(input);
                out.flush();

                if (BYE.equals(input.trim())) {
                    log.info("Disconnected from serer.");
                    break;
                }

                String response = in.readLine();
                if (Objects.isNull(response)) {
                    throw new Exception("Connection closed by server");
                }
                log.info("Server : {}", response);
            }
        } catch (Exception e) {
            log.error("Client error : ", e);
            throw new RuntimeException("Client Error : ", e);
        }
    }
}
