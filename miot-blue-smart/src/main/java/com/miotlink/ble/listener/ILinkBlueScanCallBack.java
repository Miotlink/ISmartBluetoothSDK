package com.miotlink.ble.listener;

import com.miotlink.ble.model.BleModelDevice;

public interface ILinkBlueScanCallBack {
    public void onScanDevice(BleModelDevice bleModelDevice)throws Exception;
}
