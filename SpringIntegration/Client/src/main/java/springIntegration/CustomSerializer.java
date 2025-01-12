package springIntegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import springIntegration.domain.MessageHeader;

import java.io.*;

@Slf4j
public class CustomSerializer extends AbstractByteArraySerializer {

    // 메시지를 직렬화하는 메서드
    @Override
    public byte[] serializeToByteArray(byte[] object) throws IOException {
        return object;
    }

    // OutputStream에 직렬화된 메시지를 쓰는 메서드
    @Override
    public void serialize(byte[] object, OutputStream outputStream) throws IOException {
        outputStream.write(object);
    }

    // byte[] 데이터를 역직렬화하여 메시지 byte[]로 변환하는 메서드
    @Override
    public byte[] deserializeFromByteArray(byte[] serialized) throws IOException {
        return serialized;
    }

    // InputStream에서 읽어온 데이터를 역직렬화하여 byte[]로 변환하는 메서드
    @Override
    public byte[] deserialize(InputStream inputStream) throws IOException {
        byte[] headerBytes = getBytes(inputStream, 48);
        MessageHeader header = MessageHeader.fromBytes(headerBytes);

        byte[] bodyBytes = getBytes(inputStream, header.getCommandLength());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.write(headerBytes);
        dataOutputStream.write(bodyBytes);

        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] getBytes(InputStream inputStream, int length) throws IOException {
        // java 11 이하
//        return StreamUtils.readFully(inputStream, length);

        // java 11 이상
        return inputStream.readNBytes(length);
    }
}
