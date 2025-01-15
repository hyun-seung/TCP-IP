package server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import server.config.ServerConfig;
import server.handler.ByteBufToByteArrayDecoder;
import server.handler.NettyServerHandler;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyServer_v2 implements NettyServer {

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new NettyServerHandler());
//                            pipeline.addLast(new ByteBufToByteArrayDecoder());
//                            pipeline.addLast(new SimpleChannelInboundHandler<byte[]>() {
//                                @Override
//                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
//                                    String received = new String(bytes, StandardCharsets.UTF_8);
//                                    log.info("Received : {}", received);
//
//                                    String response = "Echo - " + received;
//                                    channelHandlerContext.writeAndFlush(
//                                            Unpooled.wrappedBuffer(response.getBytes(StandardCharsets.UTF_8))
//                                    );
//                                }
//                            });
                        }
                    });

            ChannelFuture future = bootstrap.bind(ServerConfig.PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Netty Server interrupted", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
