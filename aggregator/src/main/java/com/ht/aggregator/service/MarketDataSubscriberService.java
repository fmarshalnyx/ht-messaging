
// MarketDataSubscriberService.java
package com.ht.aggregator.service;

import com.ht.aggregator.model.MarketDataTick;
import com.ht.messaging.HtMessagingProvider;
import com.ht.messaging.Serializer;
import com.ht.messaging.Subscriber;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class MarketDataSubscriberService {

    private final HtMessagingProvider messagingProvider;
    private final Serializer<MarketDataTick> serializer;
    private Subscriber subscriber;

    public MarketDataSubscriberService(HtMessagingProvider messagingProvider,
                                       Serializer<MarketDataTick> serializer) {
        this.messagingProvider = messagingProvider;
        this.serializer = serializer;
    }

    @PostConstruct
    public void init() {
        subscriber = messagingProvider.createSubscriber("aeron:udp?endpoint=localhost:40123", 10,
                tick -> System.out.println("Received: " + tick.getSymbol() + " @ " + tick.getPrice()), serializer);
        subscriber.start();
    }

    @PreDestroy
    public void cleanup() {
        subscriber.close();
        messagingProvider.shutdown();
    }
}
