package com.miotlink;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.miotlink.ble.Ble;

import com.miotlink.ble.callback.BleConnectCallback;
import com.miotlink.ble.callback.BleNotifyCallback;

import com.miotlink.ble.callback.BleScanCallback;
import com.miotlink.ble.callback.BleStatusCallback;
import com.miotlink.ble.callback.BleWriteCallback;

import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.model.BleDevice;
import com.miotlink.ble.model.BleFactory;
import com.miotlink.ble.model.BleModelDevice;
import com.miotlink.ble.model.BluetoothDeviceStore;
import com.miotlink.ble.model.ScanRecord;
import com.miotlink.ble.service.BlueISmartImpl;
import com.miotlink.ble.service.ISmart;
import com.miotlink.ble.utils.ByteUtils;

import com.miotlink.ble.utils.UuidUtils;


import com.miotlink.protocol.BluetoothMessage;
import com.miotlink.utils.HexUtil;

import java.util.UUID;

public class MiotlinkBluetoothSDK {


    private static MiotlinkBluetoothSDK instance = null;



    private Ble<BleModelDevice> ble = null;

    public static synchronized MiotlinkBluetoothSDK getInstance() {

        if (instance == null) {
            synchronized (MiotlinkBluetoothSDK.class) {
                if (instance == null) {
                    instance = new MiotlinkBluetoothSDK();
                }
            }
        }
        return instance;
    }

    private Context mContext;

    private ILinkBlueScanCallBack mBlueScanCallBack;



    private ISmart iSmart=null;

    public void setDebug(boolean isDebug){
        BlueISmartImpl.Debug=isDebug;
    }

    /**
     * 初始化参数
     * @param mContext
     * @param smartListener
     * @throws Exception
     */
    public void init(Context mContext,SmartListener smartListener) throws Exception {
        this.mContext = mContext;
        if (iSmart==null){
            iSmart=new BlueISmartImpl();
        }
        iSmart.init(mContext, smartListener);

    }

    /**
     * 扫描蓝牙设备
     * @param scanCallBack
     */
    public void startScan(ILinkBlueScanCallBack scanCallBack) {
        this.mBlueScanCallBack = scanCallBack;

        try {
            if (iSmart==null){
                iSmart=new BlueISmartImpl();
            }
            iSmart.onScan(scanCallBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String macAddress="";

    public void startSmartConfig(String macAddress, String ssid, String passowrd,int delayMillis ,ILinkSmartConfigListener linkSmartConfigListener){
        try {
            if (iSmart==null){
                iSmart=new BlueISmartImpl();
            }
            this.macAddress=macAddress;
            iSmart.onStartSmartConfig(macAddress,ssid,passowrd,delayMillis,linkSmartConfigListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStopSmartConfig(String macAddress){
        try {
            if (iSmart==null){
                iSmart=new BlueISmartImpl();
            }
            iSmart.onDisConnect(macAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkPermissions(){
        if(iSmart!=null){
            return iSmart.checkAuthority();
        }
        return 0;
    }



    public void startBluetooth(Activity activity, int requestCode) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, requestCode);
    }



    public void onStopScan() {
        if (iSmart!=null){
            try {
                iSmart.onScanStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void onDestory() {
        if (iSmart!=null){
            try {
                iSmart.onDestory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
