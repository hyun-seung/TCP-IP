package netty.service;

import io.netty.bootstrap.Bootstrap;

import java.util.concurrent.CompletableFuture;

public interface NettyClient<T> {

    CompletableFuture<Void> connect();
    CompletableFuture<Void> connectToServer(Bootstrap bootstrap) throws InterruptedException;
    CompletableFuture<Void> sendMessage(T msg);
    CompletableFuture<Void> close();
}
