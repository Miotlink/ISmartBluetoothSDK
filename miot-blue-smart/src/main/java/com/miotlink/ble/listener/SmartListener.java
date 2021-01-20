package com.miotlink.ble.listener;

public interface SmartListener {

    public void onSmartListener(int code,String message,String data)throws Exception;
}
