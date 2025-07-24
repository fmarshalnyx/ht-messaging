package com.ht.messaging.aeron;

import com.ht.messaging.Publisher;
import com.ht.messaging.Serializer;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.List;

public class AeronPublisher<T> implements Publisher<T> {
    private final Publication publication;
    private final UnsafeBuffer buffer;
    private final Serializer<T> serializer;

    public AeronPublisher(Aeron aeron, String channel, int streamId, Serializer<T> serializer) {
        this.publication = aeron.addPublication(channel, streamId);
        this.buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(1024));
        this.serializer = serializer;
    }

    @Override
    public void publish(T message) {
        serializer.setOutputTarget(buffer, 0);
        int length = serializer.serialize(message);
        while (publication.offer(buffer, 0, length) < 0) {
            Thread.yield();
        }
    }

    @Override
    public void close() {
        publication.close();
    }
}
