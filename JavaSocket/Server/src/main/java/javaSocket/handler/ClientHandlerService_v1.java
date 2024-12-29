package javaSocket.handler;

import javaSocket.config.Constant;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class ClientHandlerService_v1 implements ClientHandler {

    private final Socket clientSocket;

    private static final String BYE = "bye";

    public ClientHandlerService_v1(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            String outputLine;

            while ((inputLine = in.readLine()) != null) {
                log.info("Received : {}", inputLine);

                outputLine = "Echo - " + inputLine;
                out.println(outputLine);
                log.info("Sent : {}", outputLine);

                if (BYE.equalsIgnoreCase(inputLine.trim())) {
                    log.info("Client requested to disconnect.");
                    out.println("Goodbye :)");
                    break;
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
