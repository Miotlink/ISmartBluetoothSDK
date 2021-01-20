package com.miotlink.ble.callback;

/**
 * Created by LiuLei on 2017/10/23.
 */

public abstract class BleReadRssiCallback<T> {

    public void onReadRssiSuccess(T device, int rssi){}
}
