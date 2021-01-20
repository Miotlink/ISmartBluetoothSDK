package com.miotlink.ble.listener;

public interface SmartNotifyListener {

    public void onSmartNotifyListener(int code,int errorCode,String errorMessage,byte [] bytes)throws Exception;
}
