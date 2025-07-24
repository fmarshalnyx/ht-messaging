// MessagingConfig.java
package com.ht.aggregator.config;

import com.ht.messaging.HtMessagingProvider;
import com.ht.messaging.Serializer;
import com.ht.messaging.aeron.AeronMessagingProvider;
import com.ht.aggregator.model.MarketDataTick;
import com.ht.messaging.serialization.JacksonJsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public HtMessagingProvider messagingProvider() {
        return new AeronMessagingProvider();
    }

    @Bean
    public Serializer<MarketDataTick> marketDataSerializer() {
        return new JacksonJsonSerializer<>(MarketDataTick.class);
    }
}
