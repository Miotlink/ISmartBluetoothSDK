package com.miotlink.ble.request;

import android.bluetooth.BluetoothGattDescriptor;

import com.miotlink.ble.Ble;
import com.miotlink.ble.BleRequestImpl;
import com.miotlink.ble.annotation.Implement;
import com.miotlink.ble.callback.BleReadDescCallback;
import com.miotlink.ble.callback.BleWriteDescCallback;
import com.miotlink.ble.callback.wrapper.BleWrapperCallback;
import com.miotlink.ble.callback.wrapper.DescWrapperCallback;
import com.miotlink.ble.model.BleDevice;

import java.util.UUID;



/**
 *
 * Created by LiuLei on 2017/10/23.
 */
@Implement(DescriptorRequest.class)
public class DescriptorRequest<T extends BleDevice> implements DescWrapperCallback<T> {

    private BleReadDescCallback<T> bleReadDescCallback;
    private BleWriteDescCallback<T> bleWriteDescCallback;
    private BleWrapperCallback<T> bleWrapperCallback;

    protected DescriptorRequest() {
        bleWrapperCallback = Ble.options().bleWrapperCallback;
    }

    public boolean readDes(T device, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, BleReadDescCallback<T> callback){
        this.bleReadDescCallback = callback;
        BleRequestImpl bleRequest = BleRequestImpl.getBleRequest();
        return bleRequest.readDescriptor(device.getBleAddress(), serviceUUID, characteristicUUID, descriptorUUID);
    }

    public boolean writeDes(T device, byte[] data, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, BleWriteDescCallback<T> callback){
        this.bleWriteDescCallback = callback;
        BleRequestImpl bleRequest = BleRequestImpl.getBleRequest();
        return bleRequest.writeDescriptor(device.getBleAddress(), data, serviceUUID, characteristicUUID, descriptorUUID);
    }


    @Override
    public void onDescReadSuccess(T device, BluetoothGattDescriptor descriptor) {
        if (bleReadDescCallback != null){
            bleReadDescCallback.onDescReadSuccess(device, descriptor);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onDescReadSuccess(device, descriptor);
        }
    }

    @Override
    public void onDescReadFailed(T device, int failedCode) {
        if (bleReadDescCallback != null){
            bleReadDescCallback.onDescReadFailed(device, failedCode);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onDescReadFailed(device, failedCode);
        }
    }

    @Override
    public void onDescWriteSuccess(T device, BluetoothGattDescriptor descriptor) {
        if (bleWriteDescCallback != null){
            bleWriteDescCallback.onDescWriteSuccess(device, descriptor);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onDescWriteSuccess(device, descriptor);
        }
    }

    @Override
    public void onDescWriteFailed(T device, int failedCode) {
        if (bleWriteDescCallback != null){
            bleWriteDescCallback.onDescWriteFailed(device, failedCode);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onDescWriteFailed(device, failedCode);
        }
    }
}
