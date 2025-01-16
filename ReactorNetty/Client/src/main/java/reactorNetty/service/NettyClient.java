package reactorNetty.service;

public interface NettyClient<T> {

    void sendMessage(T msg);
}
