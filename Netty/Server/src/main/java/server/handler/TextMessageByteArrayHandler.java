package server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import server.domain.MessageHeader;
import server.domain.TextMessage;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class TextMessageByteArrayHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, 48);
        MessageHeader messageHeader = MessageHeader.fromBytes(headerBytes);

        int bodyLength = messageHeader.getCommandLength();
        byte[] bodyBytes = Arrays.copyOfRange(bytes, 48, 48 + bodyLength);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        log.info("Received : Header={}, Body={}", messageHeader, body);

        TextMessage responseTextMessage = new TextMessage(
                messageHeader.getCommandId(), messageHeader.getSessionId(), "Hello Client");
        log.info("Sent : {}", responseTextMessage);
        channelHandlerContext.writeAndFlush(Unpooled.wrappedBuffer(responseTextMessage.toBytes()));
    }
}
