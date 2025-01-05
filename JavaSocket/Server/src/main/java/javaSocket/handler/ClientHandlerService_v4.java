package javaSocket.handler;

import javaSocket.config.Constant;
import javaSocket.domain.MediaBody;
import javaSocket.domain.MediaInfo;
import javaSocket.domain.MessageHeader;
import javaSocket.domain.common.CommandId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

@Slf4j
public class ClientHandlerService_v4 implements ClientHandler {

    private final Socket clientSocket;

    public ClientHandlerService_v4(Socket socket) {
        this.clientSocket = socket;
    }

    private static byte[] getReadNBytes(InputStream inputStream, int length) throws IOException {
        // java 11 이상
        return inputStream.readNBytes(length);

        // java 11 이하
//        return StreamUtils.readFully(inputStream, length);
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
                MediaBody body = MediaBody.fromBytes(bodyBytes);

                log.info("Received : Header={}, Body={}", header, body);

                // 파일 다운로드
                List<MediaInfo> mediaInfos = body.getMediaInfos();
                if (!ObjectUtils.isEmpty(mediaInfos)) {
                    for (MediaInfo mediaInfo : mediaInfos) {
                        File file = new File(
                                "received_" + mediaInfo.getContentName().trim() + mediaInfo.getContentType().getExtension());
                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            fileOutputStream.write(mediaInfo.getContents());
                            log.info("Media file saved ad : {}", file.getAbsolutePath());
                        }
                    }
                }

                MessageHeader responseHeader = new MessageHeader(CommandId.RES, 0, header.getSessionId());
                byte[] responseHeaderBytes = responseHeader.toBytes();
                outputStream.write(responseHeaderBytes);
                outputStream.flush();

                log.info("Sent : {}", responseHeader);
            }
        } catch (Exception e) {
            if (Constant.CONNECTION_RESET.equalsIgnoreCase(e.getMessage())) {
                log.warn("Connection reset by client : {}", clientSocket.getInetAddress());
            } else {
                log.error("Error handling Client : ", e);
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
