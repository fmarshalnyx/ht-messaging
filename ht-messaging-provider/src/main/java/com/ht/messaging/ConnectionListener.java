// ConnectionListener.java
package com.ht.messaging;

public interface ConnectionListener {
    void onConnectionLost();
    void onReconnected();
}
