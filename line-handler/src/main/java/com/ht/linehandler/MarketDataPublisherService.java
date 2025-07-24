//// LineHandlerApplication.java
//package com.ht.linehandler;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.scheduling.annotation.EnableScheduling;
//
//@SpringBootApplication
//@EnableScheduling
//public class LineHandlerApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(LineHandlerApplication.class, args);
//    }
//}
//
//// MarketDataTick.java
//package com.ht.linehandler.model;
//
//public class MarketDataTick {
//    private String symbol;
//    private double price;
//    private long timestamp;
//
//    public MarketDataTick() {}
//
//    public MarketDataTick(String symbol, double price, long timestamp) {
//        this.symbol = symbol;
//        this.price = price;
//        this.timestamp = timestamp;
//    }
//
//    public String getSymbol() { return symbol; }
//    public void setSymbol(String symbol) { this.symbol = symbol; }
//
//    public double getPrice() { return price; }
//    public void setPrice(double price) { this.price = price; }
//
//    public long getTimestamp() { return timestamp; }
//    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
//}
//
//// MessagingConfig.java
//package com.ht.linehandler.config;
//
//import com.ht.messaging.HtMessagingProvider;
//import com.ht.messaging.Serializer;
//import com.ht.messaging.aeron.AeronMessagingProvider;
//import com.ht.linehandler.model.MarketDataTick;
//import com.ht.messaging.serialization.JacksonJsonSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MessagingConfig {
//
//    @Bean
//    public HtMessagingProvider messagingProvider() {
//        return new AeronMessagingProvider();
//    }
//
//    @Bean
//    public Serializer<MarketDataTick> marketDataSerializer() {
//        return new JacksonJsonSerializer<>(MarketDataTick.class);
//    }
//}
//
//// MarketDataPublisherService.java
package com.ht.linehandler.service;

import com.ht.linehandler.model.MarketDataTick;
import com.ht.messaging.HtMessagingProvider;
import com.ht.messaging.Publisher;
import com.ht.messaging.Serializer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MarketDataPublisherService {

    private final HtMessagingProvider messagingProvider;
    private final Serializer<MarketDataTick> serializer;
    private Publisher<MarketDataTick> publisher;
    private final Random random = new Random();

    public MarketDataPublisherService(HtMessagingProvider messagingProvider,
                                      Serializer<MarketDataTick> serializer) {
        this.messagingProvider = messagingProvider;
        this.serializer = serializer;
    }

    @PostConstruct
    public void init() {
        publisher = messagingProvider.createPublisher("aeron:udp?endpoint=localhost:40123", 10, serializer);
    }

    @Scheduled(fixedRate = 1000)
    public void publishTick() {
        MarketDataTick tick = new MarketDataTick("AAPL", 100 + random.nextDouble() * 10, System.currentTimeMillis());
        publisher.publish(tick);
    }

    @PreDestroy
    public void cleanup() {
        publisher.close();
        messagingProvider.shutdown();
    }
}
