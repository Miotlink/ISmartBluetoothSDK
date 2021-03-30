package com.miotlink.ble.listener;

import com.miotlink.ble.model.BleEntityData;

public interface SmartNotifyListener {

    public void onSmartNotifyListener(int errorCode, String errorMessage, BleEntityData entityData)throws Exception;
}
