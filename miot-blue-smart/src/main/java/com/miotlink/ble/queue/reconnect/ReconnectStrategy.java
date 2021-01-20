package com.miotlink.ble.queue.reconnect;

/**
 * author: jerry
 * date: 20-11-30
 * email: superliu0911@gmail.com
 * des:
 */
public class ReconnectStrategy {
    public int times;//重连次数
    public long delay;//重连间隔
    public boolean reconnectIfOpenBluetooth;//重新打开蓝牙时是否重连

    private ReconnectStrategy(){}

    public static final class Builder {
        public int times = -1;//重连次数
        public long delay = DefaultReConnectHandler.DEFAULT_CONNECT_DELAY;//重连间隔
        public boolean reconnectIfOpenBluetooth = true;//重新打开蓝牙时是否重连

        Builder() {
        }

        public Builder times(int times) {
            this.times = times;
            return this;
        }

        public Builder delay(long delay) {
            this.delay = delay;
            return this;
        }

        public Builder reconnectIfOpenBluetooth(boolean reconnectIfOpenBluetooth) {
            this.reconnectIfOpenBluetooth = reconnectIfOpenBluetooth;
            return this;
        }

        public ReconnectStrategy build() {
            ReconnectStrategy reconnectStrategy = new ReconnectStrategy();
            reconnectStrategy.times = this.times;
            reconnectStrategy.delay = this.delay;
            reconnectStrategy.reconnectIfOpenBluetooth = this.reconnectIfOpenBluetooth;
            return reconnectStrategy;
        }
    }
}
