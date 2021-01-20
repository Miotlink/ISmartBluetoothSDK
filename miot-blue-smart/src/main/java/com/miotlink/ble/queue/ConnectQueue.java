package com.miotlink.ble.queue;

import android.support.annotation.NonNull;

import com.miotlink.ble.BleLog;
import com.miotlink.ble.queue.reconnect.DefaultReConnectHandler;


public final class ConnectQueue extends Queue{

    private static volatile ConnectQueue sInstance;

    private ConnectQueue() {
    }

    @NonNull
    public static ConnectQueue getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (ConnectQueue.class) {
            if (sInstance == null) {
                sInstance = new ConnectQueue();
            }
        }
        return sInstance;
    }

    @Override
    public void execute(RequestTask requestTask) {
        boolean reconnect = DefaultReConnectHandler.provideReconnectHandler().reconnect(requestTask.getAddress());
        BleLog.i("ConnectQueue", "正在重新连接设备:>>>>>>>result:"+reconnect+">>>"+requestTask.getAddress());
    }

}
