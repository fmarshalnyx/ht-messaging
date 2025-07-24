package com.ht.messaging;

import org.agrona.concurrent.UnsafeBuffer;

public interface Serializer<T> {
    void setOutputTarget(UnsafeBuffer buffer, int offset);
    int serialize(T obj);
    void setInputSource(UnsafeBuffer buffer, int offset, int length);
    T deserialize();

    default int serialize(T obj, UnsafeBuffer buffer, int offset) {
        setOutputTarget(buffer, offset);
        return serialize(obj);
    }

    default T deserialize(UnsafeBuffer buffer, int offset, int length) {
        setInputSource(buffer, offset, length);
        return deserialize();
    }
}
