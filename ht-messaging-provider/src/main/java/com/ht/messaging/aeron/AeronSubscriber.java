package com.ht.messaging.aeron;

import com.ht.messaging.*;
        import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.List;

public class AeronSubscriber<T> implements Subscriber {
    private final Subscription subscription;
    private final Thread pollingThread;
    private final IdleStrategy idleStrategy = new SleepingIdleStrategy();
    private volatile boolean running = true;

    public AeronSubscriber(Aeron aeron, String channel, int streamId, HtMessagingProvider.MessageHandler<T> handler, Serializer<T> serializer, List<ConnectionListener> listeners) {
        this.subscription = aeron.addSubscription(channel, streamId);

        this.pollingThread = new Thread(() -> {
            FragmentHandler fragmentHandler = (buffer, offset, length, header) -> {
                serializer.setInputSource(buffer, offset, length);
                T obj = serializer.deserialize();
                handler.onMessage(obj);
            };
            while (running) {
                int fragments = subscription.poll(fragmentHandler, 10);
                if (fragments == 0) idleStrategy.idle();
            }
        }, "Aeron-Subscriber");
    }

    @Override
    public void start() {
        pollingThread.start();
    }

    @Override
    public void close() {
        running = false;
        subscription.close();
    }
}
