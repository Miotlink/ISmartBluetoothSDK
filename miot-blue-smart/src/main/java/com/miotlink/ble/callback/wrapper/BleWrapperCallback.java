package com.miotlink.ble.callback.wrapper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.miotlink.ble.callback.BleScanCallback;
import com.miotlink.ble.model.BleDevice;


public abstract class BleWrapperCallback<T extends BleDevice> extends BleScanCallback<T>
        implements ConnectWrapperCallback<T>, NotifyWrapperCallback<T>, WriteWrapperCallback<T>,
        ReadWrapperCallback<T>,DescWrapperCallback<T>,MtuWrapperCallback<T>{

    private static final String TAG = "BleWrapperCallback";

    @Override
    public void onConnectionChanged(T device) {
    }

    @Override
    public void onConnectFailed(T device, int errorCode) {

    }

    @Override
    public void onReady(T device) {
    }

    @Override
    public void onServicesDiscovered(T device, BluetoothGatt gatt) {

    }

    @Override
    public void onChanged(T device, BluetoothGattCharacteristic characteristic) {
    }

    @Override
    public void onNotifySuccess(T device) {

    }

    @Override
    public void onNotifyCanceled(T device) {

    }

    @Override
    public void onWriteSuccess(T device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onWriteFailed(T device, int failedCode) {

    }

    @Override
    public void onReadSuccess(T device, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onReadFailed(T device, int failedCode) {

    }

    @Override
    public void onLeScan(T device, int rssi, byte[] scanRecord) {
    }

    @Override
    public void onDescReadSuccess(T device, BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onDescReadFailed(T device, int failedCode) {

    }

    @Override
    public void onDescWriteSuccess(T device, BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onDescWriteFailed(T device, int failedCode) {

    }

    @Override
    public void onMtuChanged(T device, int mtu, int status) {

    }
}
