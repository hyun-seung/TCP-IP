package javaSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) throws InterruptedException {
        int totalRequests = 500;
        int currentClients = 50;

        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicLong totalResponseTime = new AtomicLong();

        AtomicInteger errorCount = new AtomicInteger();

        String message = "Hello, Server!";

        log.info("START !!");

        long startTime = System.currentTimeMillis();

        for (int i=0; i<currentClients; i++) {
            new Thread(() -> {
                int requestsForThread = totalRequests/currentClients;
                int successfulRequests = 0;

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress("127.0.0.1", 12345), 3000);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    for (int j=0; j<requestsForThread; j++) {
                        try {
                            long requestStart = System.currentTimeMillis();

                            writer.write(message + "\r\n");
                            writer.flush();

                            String response = reader.readLine();
                            log.info("{} / {}", response, latch.getCount());

                            long requestEnd = System.currentTimeMillis();
                            totalResponseTime.addAndGet(requestEnd - requestStart);

                            successfulRequests += 1;
                        } catch (Exception e) {
                            log.error("#1 {}", e.getMessage());
                            errorCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    }
                } catch (Exception e) {
                    log.error("#2 {}", e.getMessage());

                    int failedRequests = requestsForThread - successfulRequests;
                    errorCount.addAndGet(failedRequests);
                    for (int k=0; k<failedRequests; k++) {
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
