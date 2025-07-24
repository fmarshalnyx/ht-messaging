// HtMessagingProvider.java
package com.ht.messaging;

import org.agrona.concurrent.UnsafeBuffer;

public interface HtMessagingProvider {
    <T> Publisher<T> createPublisher(String channel, int streamId, Serializer<T> serializer);
    <T> Subscriber createSubscriber(String channel, int streamId, MessageHandler<T> handler, Serializer<T> serializer);
    void registerConnectionListener(ConnectionListener listener);
    void shutdown();

    interface MessageHandler<T> {
        void onMessage(T message);
    }
}
