package com.miotlink.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

import com.miotlink.ble.Ble;
import com.miotlink.ble.BleRequestImpl;
import com.miotlink.ble.annotation.Implement;
import com.miotlink.ble.callback.BleReadCallback;
import com.miotlink.ble.callback.wrapper.BleWrapperCallback;
import com.miotlink.ble.callback.wrapper.ReadWrapperCallback;
import com.miotlink.ble.model.BleDevice;

import java.util.UUID;



/**
 *
 * Created by LiuLei on 2017/10/23.
 */
@Implement(ReadRequest.class)
public class ReadRequest<T extends BleDevice> implements ReadWrapperCallback<T> {

    private BleReadCallback<T> bleReadCallback;
    private BleWrapperCallback<T> bleWrapperCallback;

    protected ReadRequest() {
        bleWrapperCallback = Ble.options().bleWrapperCallback;
    }

    public boolean read(T device, BleReadCallback<T> callback){
        this.bleReadCallback = callback;
        BleRequestImpl bleRequest = BleRequestImpl.getBleRequest();
        return bleRequest.readCharacteristic(device.getBleAddress());
    }

    public boolean readByUuid(T device, UUID serviceUUID, UUID characteristicUUID, BleReadCallback<T> callback){
        this.bleReadCallback = callback;
        BleRequestImpl bleRequest = BleRequestImpl.getBleRequest();
        return bleRequest.readCharacteristicByUuid(device.getBleAddress(), serviceUUID, characteristicUUID);
    }

    @Override
    public void onReadSuccess(T device, BluetoothGattCharacteristic characteristic) {
        if(bleReadCallback != null){
            bleReadCallback.onReadSuccess(device, characteristic);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onReadSuccess(device, characteristic);
        }
    }

    @Override
    public void onReadFailed(T device, int failedCode) {
        if(bleReadCallback != null){
            bleReadCallback.onReadFailed(device, failedCode);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onReadFailed(device, failedCode);
        }
    }
}
