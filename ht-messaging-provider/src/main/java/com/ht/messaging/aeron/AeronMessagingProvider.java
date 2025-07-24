package com.ht.messaging.aeron;

import com.ht.messaging.*;
import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.agrona.CloseHelper;

import java.util.ArrayList;
import java.util.List;

public class AeronMessagingProvider implements HtMessagingProvider {
    private final List<ConnectionListener> listeners = new ArrayList<>();
    private MediaDriver mediaDriver;
    private Aeron aeron;

    public AeronMessagingProvider() {
        try {
            mediaDriver = MediaDriver.launchEmbedded();
            aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Aeron", e);
        }
    }

    @Override
    public <T> Publisher<T> createPublisher(String channel, int streamId, Serializer<T> serializer) {
        return new AeronPublisher<>(aeron, channel, streamId, serializer);
    }

    @Override
    public <T> Subscriber createSubscriber(String channel, int streamId, MessageHandler<T> handler, Serializer<T> serializer) {
        return new AeronSubscriber<>(aeron, channel, streamId, handler, serializer, listeners);
    }

    @Override
    public void registerConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void shutdown() {
        CloseHelper.quietClose(aeron);
        CloseHelper.quietClose(mediaDriver);
    }
}

//// AeronPublisher.java
//package com.ht.messaging.aeron;
//
//import com.ht.messaging.Publisher;
//import com.ht.messaging.Serializer;
//import io.aeron.Aeron;
//import io.aeron.Publication;
//import org.agrona.concurrent.UnsafeBuffer;
//
//import java.nio.ByteBuffer;
//
//public class AeronPublisher<T> implements Publisher<T> {
//    private final Publication publication;
//    private final UnsafeBuffer buffer;
//    private final Serializer<T> serializer;
//
//    public AeronPublisher(Aeron aeron, String channel, int streamId, Serializer<T> serializer) {
//        this.publication = aeron.addPublication(channel, streamId);
//        this.buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(1024));
//        this.serializer = serializer;
//    }
//
//    @Override
//    public void publish(T message) {
//        serializer.setOutputTarget(buffer, 0);
//        int length = serializer.serialize(message);
//        while (publication.offer(buffer, 0, length) < 0) {
//            Thread.yield();
//        }
//    }
//
//    @Override
//    public void close() {
//        publication.close();
//    }
//}
//
//// AeronSubscriber.java
//package com.ht.messaging.aeron;
//
//import com.ht.messaging.*;
//        import io.aeron.Aeron;
//import io.aeron.Subscription;
//import io.aeron.logbuffer.FragmentHandler;
//import org.agrona.concurrent.IdleStrategy;
//import org.agrona.concurrent.SleepingIdleStrategy;
//
//import java.util.List;
//
//public class AeronSubscriber<T> implements Subscriber {
//    private final Subscription subscription;
//    private final Thread pollingThread;
//    private final IdleStrategy idleStrategy = new SleepingIdleStrategy();
//    private volatile boolean running = true;
//
//    public AeronSubscriber(Aeron aeron, String channel, int streamId, HtMessagingProvider.MessageHandler<T> handler, Serializer<T> serializer, List<ConnectionListener> listeners) {
//        this.subscription = aeron.addSubscription(channel, streamId);
//
//        this.pollingThread = new Thread(() -> {
//            FragmentHandler fragmentHandler = (buffer, offset, length, header) -> {
//                serializer.setInputSource(buffer, offset, length);
//                T obj = serializer.deserialize();
//                handler.onMessage(obj);
//            };
//            while (running) {
//                int fragments = subscription.poll(fragmentHandler, 10);
//                if (fragments == 0) idleStrategy.idle();
//            }
//        }, "Aeron-Subscriber");
//    }
//
//    @Override
//    public void start() {
//        pollingThread.start();
//    }
//
//    @Override
//    public void close() {
//        running = false;
//        subscription.close();
//    }
//}
