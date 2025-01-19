package springIntegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) throws Exception {
        TcpNioClientConnectionFactory connectionFactory = new TcpNioClientConnectionFactory("127.0.0.1", 12345);
        connectionFactory.setSingleUse(true);
        connectionFactory.setUsingDirectBuffers(true);
        connectionFactory.setConnectTimeout(3);
        connectionFactory.setSoKeepAlive(true);
        connectionFactory.setSoTcpNoDelay(true);
        connectionFactory.start();

        TcpOutboundGateway gateway = new TcpOutboundGateway();
        gateway.setConnectionFactory(connectionFactory);

        DirectChannel outputChannel = new DirectChannel();
        gateway.setOutputChannel(outputChannel);

        QueueChannel replyChannel = new QueueChannel();
        gateway.setReplyChannel(replyChannel);

        outputChannel.subscribe(message -> {
            try {
                gateway.handleMessage(message);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });

        gateway.start();

        Thread.sleep(3000);

        String message = "Hello, Server!";

        int totalRequests = 500;
        int currentClients = 50;

        AtomicLong totalResponseTime = new AtomicLong();
        AtomicInteger errorCount = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(totalRequests);

        long startTime = System.currentTimeMillis();

        for (int i=0; i<currentClients; i++) {
            new Thread(() -> {
                for (int j=0; j<totalRequests/currentClients; j++) {
                    long requestStart = System.currentTimeMillis();

                    try {
                        Message<String> requestMessage = MessageBuilder.withPayload(message)
                                .setReplyChannel(replyChannel)
                                .build();
                        outputChannel.send(requestMessage);

                        Message<?> responseMessage = replyChannel.receive(3000);
                        if (responseMessage == null) {
                            log.error("No response received");
                        }

                        long requestEnd = System.currentTimeMillis();
                        totalResponseTime.addAndGet(requestEnd - requestStart);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }

        latch.await();

        long endTime = System.currentTimeMillis();

        double durationSeconds = (endTime - startTime) / 1000.0;
        double tps = totalRequests / durationSeconds;
        double avgResponseTime = totalResponseTime.get() / (double) totalRequests;

        log.info("Total Requests : {}", totalRequests);
        log.info("Total Connection : {}", currentClients);
        log.info("Total Duration : {}", (durationSeconds * 1000) + " ms");
        log.info("TPS : {}", tps);
        log.info("Average Response Time : {}", avgResponseTime + " ms");
        log.info("Errors : {}", errorCount.get());
    }
}
