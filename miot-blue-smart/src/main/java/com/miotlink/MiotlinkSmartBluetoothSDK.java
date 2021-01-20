package com.miotlink;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.miotlink.ble.Ble;

import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.model.BleModelDevice;
import com.miotlink.ble.service.BlueISmartImpl;
import com.miotlink.ble.service.ISmart;

public class MiotlinkSmartBluetoothSDK {


    private static MiotlinkSmartBluetoothSDK instance = null;



    private Ble<BleModelDevice> ble = null;

    public static synchronized MiotlinkSmartBluetoothSDK getInstance() {

        if (instance == null) {
            synchronized (MiotlinkSmartBluetoothSDK.class) {
                if (instance == null) {
                    instance = new MiotlinkSmartBluetoothSDK();
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
