
package com.ht.messaging;

public interface Publisher<T> {
    void publish(T message);
    void close();
}
