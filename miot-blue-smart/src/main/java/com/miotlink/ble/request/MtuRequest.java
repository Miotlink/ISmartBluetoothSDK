package com.miotlink.ble.request;


import com.miotlink.ble.Ble;
import com.miotlink.ble.BleRequestImpl;
import com.miotlink.ble.annotation.Implement;
import com.miotlink.ble.callback.BleMtuCallback;
import com.miotlink.ble.callback.wrapper.BleWrapperCallback;
import com.miotlink.ble.callback.wrapper.MtuWrapperCallback;
import com.miotlink.ble.model.BleDevice;

/**
 *
 * Created by LiuLei on 2017/10/23.
 */
@Implement(MtuRequest.class)
public class MtuRequest<T extends BleDevice> implements MtuWrapperCallback<T> {

    private BleMtuCallback<T> bleMtuCallback;
    private BleWrapperCallback<T> bleWrapperCallback;

    protected MtuRequest() {
        bleWrapperCallback = Ble.options().bleWrapperCallback;
    }

    public boolean setMtu(String address, int mtu, BleMtuCallback<T> callback){
        this.bleMtuCallback = callback;
        BleRequestImpl bleRequest = BleRequestImpl.getBleRequest();
        return bleRequest.setMtu(address, mtu);
    }

    @Override
    public void onMtuChanged(T device, int mtu, int status) {
        if(null != bleMtuCallback){
            bleMtuCallback.onMtuChanged(device, mtu, status);
        }

        if (bleWrapperCallback != null){
            bleWrapperCallback.onMtuChanged(device, mtu, status);
        }
    }
}
