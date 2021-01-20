package com.miotlink.ble.listener;

import com.miotlink.ble.model.BleModelDevice;

public interface ILinkConnectCallback {

    public void onConnectedReceiver(BleModelDevice bleModelDevice)throws Exception;


    public void onDisConnectedRecevice()throws Exception;
}
