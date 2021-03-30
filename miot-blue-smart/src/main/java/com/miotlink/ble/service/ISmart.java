package com.miotlink.ble.service;

import android.content.Context;

import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkConnectCallback;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.listener.SmartNotifyListener;

public interface ISmart {

    /**
     * 初始化参数信息
     * @param mContext
     * @throws Exception
     */
    public void init(Context mContext, SmartListener mSmartListener)throws Exception;

    public int checkAuthority();

    public void setServiceUUID(String serviceUuId,String readUuid,String writeUuid)throws Exception;

    /**
     * 扫描妙联蓝牙设备
     * @throws Exception
     */
    public void onScan(ILinkBlueScanCallBack mILinkBlueScanCallBack)throws Exception;

    public void openBluetooth();

    /**
     * 停止扫描蓝牙设备
     * @throws Exception
     */
    public void onScanStop()throws Exception;

    /**
     * 根据MAC地址连接蓝牙
     * @param macCode
     * @throws Exception
     */
    public void onConnect(String macCode, ILinkConnectCallback mLinkConnectCallback)throws Exception;

    /**
     * 开启配网信息
     * @param ssid
     * @param password
     * @throws Exception
     */
    public void onStartSmartConfig(String macCode, String ssid , String password,int delayMillis, ILinkSmartConfigListener mILinkSmartConfigListener)throws Exception;

    /**
     * 发送串口数据
     * @param mac
     * @param data
     * @throws Exception
     */
    public void sendUartData(String mac, byte[] data, SmartNotifyListener smartNotifyListener)throws Exception;


    public void onDisConnect(String macCode)throws Exception;

    public void onDestory()throws Exception;

}
