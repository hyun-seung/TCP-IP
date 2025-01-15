package netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import netty.domain.MessageHeader;
import netty.domain.TextMessage;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class TextMessageByteArrayHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, 48);
        MessageHeader header = MessageHeader.fromBytes(headerBytes);

        int bodyLength = header.getCommandLength();
        byte[] bodyBytes = Arrays.copyOfRange(bytes, 48, 48 + bodyLength);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        TextMessage textMessage = new TextMessage(header, body);
        log.info("Received : {}", textMessage);
    }
}
