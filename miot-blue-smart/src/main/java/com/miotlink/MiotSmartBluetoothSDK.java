package com.miotlink;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.listener.SmartNotifyListener;
import com.miotlink.protocol.BlueISmartImpl;
import com.miotlink.ble.service.ISmart;
import com.miotlink.utils.IBluetooth;

public class MiotSmartBluetoothSDK {


    private static volatile MiotSmartBluetoothSDK instance = null;





    public static synchronized MiotSmartBluetoothSDK getInstance() {

        if (instance == null) {
            synchronized (MiotSmartBluetoothSDK.class) {
                if (instance == null) {
                    instance = new MiotSmartBluetoothSDK();
                }
            }
        }
        return instance;
    }





    private ISmart iSmart=null;

    public void setDeviceInfo(boolean isOpen){

        if (iSmart!=null){
            iSmart.setDeviceInfo(isOpen);
        }
    }

    /**
     * 打印数据
     * @param isDebug
     */
    public static void setDebug(boolean isDebug){
        BlueISmartImpl.Debug=isDebug;
    }

    /**
     * 设置UUID  参数
     * @param serverUuid
     * @param writeUuid
     * @param readUuid
     * @param notifyUuid
     */
    public static void setServerUUID(String serverUuid,String writeUuid,String readUuid,String notifyUuid){
        if (!TextUtils.isEmpty(serverUuid)){
            IBluetooth.SERVER_UUID =serverUuid;
        }
        if (!TextUtils.isEmpty(writeUuid)){
            IBluetooth.SERVER_WRITE_UUID=writeUuid;
        }
        if (!TextUtils.isEmpty(readUuid)){
            IBluetooth.SERVER_READ_UUID=readUuid;
        }
        if (!TextUtils.isEmpty(notifyUuid)){
            IBluetooth.SERVER_NOTIFY_UUID=notifyUuid;
        }
    }

    public static void setBleFilterName(String name){
        IBluetooth.FILTER_NAME=name;
    }


    /**
     * 初始化参数
     * @param mContext
     * @param smartListener
     * @throws Exception
     */
    public void init(Context mContext,String appKey,SmartListener smartListener)  {
        try {
            if (iSmart==null){
                iSmart=new BlueISmartImpl();
            }
            iSmart.init(mContext, smartListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 扫描蓝牙设备
     * @param scanCallBack
     */
    public void startScan(final ILinkBlueScanCallBack scanCallBack) {
        if (iSmart==null){
            iSmart=new BlueISmartImpl();
        }
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    iSmart.onScan(scanCallBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private String macAddress="";

    /**
     *
     * @param macCode  mac地址
     * @param ssid  路由器账户
     * @param passowrd 路由器密码
     * @param delayMillis 设置超时时间 默认60s
     * @param linkSmartConfigListener
     */
    public void startSmartConfig(String macCode, String ssid, String passowrd,int delayMillis,ILinkSmartConfigListener linkSmartConfigListener){
        try {
            if (iSmart==null){
                iSmart=new BlueISmartImpl();
            }
            this.macAddress=macCode;
            iSmart.onStartSmartConfig(macCode,ssid,passowrd,delayMillis,linkSmartConfigListener);
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

    /**
     * 检查蓝牙权限
     * @return
     * 2 该手机不支持蓝牙
     */
    public int checkPermissions(){
        if(iSmart!=null){
            return iSmart.checkAuthority();
        }
        return 0;
    }

    /**
     * 强制打开蓝牙开关
     */
    public void turnOnBlueToothNo(){
        if (iSmart!=null){
            iSmart.openBluetooth();
        }

    }



    public void startBluetooth(Activity activity, int requestCode) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, requestCode);
    }

    public void send(String macCode, byte [] bytes, SmartNotifyListener smartNotifyListener){
        if (iSmart!=null){
            try {
                iSmart.sendUartData(macCode,bytes,smartNotifyListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public boolean onDisConnect(){
        if (iSmart!=null){
            try {
                iSmart.onDisConnect("");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
