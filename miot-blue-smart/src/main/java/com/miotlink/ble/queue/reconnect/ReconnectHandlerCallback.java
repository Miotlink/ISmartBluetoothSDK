package com.miotlink.ble.queue.reconnect;

/**
 * author: jerry
 * date: 20-11-30
 * email: superliu0911@gmail.com
 * des:
 */
public interface ReconnectHandlerCallback<T> {
//    ReconnectStrategy strategy();
    void onConnectionChanged(T device);
}
