package javaSocket.handler;

import javaSocket.domain.MediaBody;
import javaSocket.domain.MediaInfo;
import javaSocket.domain.MediaMessage;
import javaSocket.domain.MessageHeader;
import javaSocket.domain.common.CommandId;
import javaSocket.domain.common.ContentType;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServerHandlerService_v4 implements ServerHandler {

    private final Socket serverSocket;

    public ServerHandlerService_v4(Socket socket) {
        this.serverSocket = socket;
    }

    private static MediaInfo getMediaInfo(File file, byte[] mediaData, int contentId, ContentType contentType) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();

            log.info("[{}] 가로 크기 : {}px, 세로 크기 : {}px", file.getName(), width, height);

            int dotIndex = file.getName().lastIndexOf('.');
            String fileName = file.getName().substring(0, dotIndex);

            // TODO : contentType 자동 추출 추가

            return new MediaInfo(contentId, contentType, width, height, fileName, mediaData);
        } else {
            log.error("이미지를 읽을 수 없습니다. 형식이 지원되지 않을 수 있습니다.");
            return new MediaInfo(contentId, contentType, 0, 0, file.getName(), mediaData);
        }
    }

    private static byte[] getReadNBytes(InputStream inputStream, int length) throws IOException {
        // java 11 이상
        return inputStream.readNBytes(length);

        // java 11 이하
//        return StreamUtils.readFully(inputStream, length);
    }

    @Override
    public void run() {
        String filePath1 = "/Users/hyunseung/Downloads/wallpaper1.jpg";
        File file1 = new File(filePath1);
        if (!file1.exists() || !file1.isFile()) {
            log.error("Invalid file : " + filePath1);
            return;
        }

        String filePath2 = "/Users/hyunseung/Downloads/wallpaper2.jpg";
        File file2 = new File(filePath2);
        if (!file2.exists() || !file2.isFile()) {
            log.error("Invalid file : " + filePath2);
            return;
        }

        try (
                InputStream inputStream = serverSocket.getInputStream();
                OutputStream outputStream = serverSocket.getOutputStream();
        ) {
            byte[] mediaData1 = Files.readAllBytes(Paths.get(filePath1));
            byte[] mediaData2 = Files.readAllBytes(Paths.get(filePath2));

            List<MediaInfo> mediaInfos = new ArrayList<>();
            mediaInfos.add(getMediaInfo(file1, mediaData1, 1, ContentType.JPG));
            mediaInfos.add(getMediaInfo(file2, mediaData2, 2, ContentType.JPG));

            MediaBody mediaBody = new MediaBody(mediaInfos);
            MediaMessage mediaMessage = new MediaMessage(CommandId.REQ, "TestMessage-20240106_01", mediaBody);

            outputStream.write(mediaMessage.toBytes());
            outputStream.flush();
            log.info("Sent : {}", mediaMessage);

            byte[] headerBytes = getReadNBytes(inputStream, 48);
            MessageHeader header = MessageHeader.fromBytes(headerBytes);

            log.info("Received : {}", header);
        } catch (Exception e) {
            log.error("Client error : ", e);
            throw new RuntimeException("Client Error : ", e);
        }
    }
}
