package com.miotlink.ble.callback;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 *
 * Created by LiuLei on 2017/10/23.
 */

public abstract class BleWriteCallback<T> {
    public abstract void onWriteSuccess(T device, BluetoothGattCharacteristic characteristic);

    public void onWriteFailed(T device, int failedCode){}
}
