package com.miotlink.ble.callback;


import com.miotlink.ble.model.BleDevice;

/**
 * Created by LiuLei on 2018/6/2.
 */

public abstract class BleMtuCallback<T> {

    public void onMtuChanged(T device, int mtu, int status){}

}
