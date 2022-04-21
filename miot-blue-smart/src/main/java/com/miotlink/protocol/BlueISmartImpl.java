package com.miotlink.protocol;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.text.TextUtils;

import com.bluetooth.sdk.R;
import com.miotlink.ble.Ble;
import com.miotlink.ble.BleLog;
import com.miotlink.ble.callback.BleConnectCallback;
import com.miotlink.ble.callback.BleMtuCallback;
import com.miotlink.ble.callback.BleNotifyCallback;
import com.miotlink.ble.callback.BleScanCallback;
import com.miotlink.ble.callback.BleStatusCallback;
import com.miotlink.ble.callback.BleWriteCallback;
import com.miotlink.ble.callback.BleWriteEntityCallback;
import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkConnectCallback;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.listener.SmartNotifyListener;
import com.miotlink.ble.model.BleDevice;
import com.miotlink.ble.model.BleEntityData;
import com.miotlink.ble.model.BleFactory;
import com.miotlink.ble.model.BleModelDevice;
import com.miotlink.ble.model.BluetoothDeviceStore;
import com.miotlink.ble.model.ScanRecord;
import com.miotlink.ble.service.ISmart;
import com.miotlink.ble.utils.ByteUtils;
import com.miotlink.ble.utils.Utils;
import com.miotlink.ble.utils.UuidUtils;
import com.miotlink.utils.IBluetooth;
import com.miotlink.utils.HexUtil;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class BlueISmartImpl extends BleWriteCallback<BleModelDevice> implements ISmart {
    //    private static final String filter_name = "MLink";
    private Ble<BleModelDevice> ble = null;
    private SmartListener mSmartListener = null;
    private Context mContext = null;
    public static boolean Debug = false;
    private BluetoothDeviceStore bluetoothDeviceStore = new BluetoothDeviceStore();


    private String ssid = "";
    private String password = "";
    private String macCode="";
    private ILinkBlueScanCallBack mILinkBlueScanCallBack = null;
    private ILinkSmartConfigListener mILinkSmartConfigListener = null;
    private SmartNotifyListener smartNotifyListener=null;
    private int errorCode= IBluetooth.Constant.ERROR_INIT_CODE;
    private String errorMessage="";

    private String deviceName="";

    private boolean isOpen;

    private BleModelDevice bleModelDevice=null;

    private MyGetDeviceInfoThread myGetDeviceInfoThread=null;

    @Override
    public void init(Context mContext, SmartListener mSmartListener) throws Exception {
        this.mContext = mContext;
        this.mSmartListener = mSmartListener;

        BleFactory bleFactory = new BleFactory<BleModelDevice>() {
            @Override
            public BleModelDevice create(String address, String name) {
                return new BleModelDevice(address, name);
            }
        };
        Ble.options()
                .setLogBleEnable(Debug)//设置是否输出打印蓝牙日志
                .setThrowBleException(true)//设置是否抛出蓝牙异常
                .setLogTAG("Mlink_BLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(true)//设置是否自动连接
                .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
                .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
                .setConnectTimeout(10 * 1000)//设置连接超时时长
                .setScanPeriod(30 * 60 * 1000)//设置扫描时长
                .setMaxConnectNum(7)//最大连接数量
                .setUuidService(UUID.fromString(UuidUtils.uuid16To128(IBluetooth.SERVER_UUID)))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128(IBluetooth.SERVER_WRITE_UUID)))//设置可写特征的uuid
                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128(IBluetooth.SERVER_READ_UUID)))//设置可读特征的uuid （选填）
                .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128(IBluetooth.SERVER_NOTIFY_UUID)))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
                .setFactory(bleFactory)
                .create(mContext, initCallback);
    }

    @Override
    public int checkAuthority() {
        if (!Ble.getInstance().isSupportBle(mContext)) {
            return 2;
        }
        if (!Ble.getInstance().isBleEnable()) {
            return 3;
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Utils.isGpsOpen(mContext)){
            return 4;
        }else if (!Utils.isPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)&&
                !Utils.isPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)){
            return 5;
        }
        return 1;

    }

    @Override
    public void setDeviceInfo(boolean isOpen) {
        this.isOpen=isOpen;

    }


    @Override
    public void setServiceUUID(String serviceUuId, String readUuid, String writeUuid) throws Exception {
        Ble.options().setUuidService(UUID.fromString(UuidUtils.uuid16To128(serviceUuId)))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128(writeUuid)))//设置可写特征的uuid
                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128(readUuid)))//设置可读特征的uuid （选填）
                .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128(readUuid)));//设置可通知特征的uuid （
    }



    Ble.InitCallback initCallback = new Ble.InitCallback() {

        @Override
        public void success() {
            if (mSmartListener != null) {
                try {
                    mSmartListener.onSmartListener(1, "init success", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void failed(int failedCode) {

            if (mSmartListener != null) {
                try {
                    mSmartListener.onSmartListener(-1, "init failed", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onScan(ILinkBlueScanCallBack mILinkBlueScanCallBack) throws Exception {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        if (!ble.isSupportBle(mContext)) {
            throw new Exception("该手机暂不支持蓝牙设备");
        }
        if (!ble.isBleEnable()) {
            throw new Exception("蓝牙访问权限未打开");
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Utils.isGpsOpen(mContext)){
            throw new Exception("Android 操作系统8.1以上未打开定位服务");
        }else if (!Utils.isPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)&&
                !Utils.isPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)){
            throw new Exception("Android 操作系统8.0以上未打开定位权限");
        }
        bluetoothDeviceStore.clear();
        this.mILinkBlueScanCallBack = mILinkBlueScanCallBack;
        if (ble.isScanning()){
            ble.stopScan();
        }
        if (!ble.isBleEnable()) {
            ble.setBleStatusCallback(new BleStatusCallback() {
                @Override
                public void onBluetoothStatusChanged(boolean isOn) {
                    if (isOn) {
                        ble.startScan(bleScanCallback);
                    }
                }
            });
            return;
        }
        ble.startScan(bleScanCallback);
    }

    BleScanCallback<BleModelDevice> bleScanCallback = new BleScanCallback<BleModelDevice>() {
        @Override
        public void onLeScan(BleModelDevice device, int rssi, byte[] scanRecord) {
            synchronized (ble.getLocker()) {
                if (bluetoothDeviceStore.getDeviceMap().containsKey(device.getMacAddress())
                        || bluetoothDeviceStore.getDeviceMap().containsKey(device.getBleAddress())) {
                    return;
                }
                if (!TextUtils.isEmpty(device.getBleName())
                        && device.getBleName().startsWith(IBluetooth.FILTER_NAME)
                        || HexUtil.encodeHexStr(scanRecord).contains("6667")
                        || !TextUtils.isEmpty(device.getBleName())
                        &&device.getBleName().startsWith("Hi-Huawei-Mars")) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            device.setScanRecord(ScanRecord.parseFromBytes(scanRecord));
                        }
                        bluetoothDeviceStore.addDevice(device);
                        if (mILinkBlueScanCallBack!=null){
                            mILinkBlueScanCallBack.onScanDevice(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };

    @Override
    public void openBluetooth() {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        ble.turnOnBlueToothNo();

    }

    @Override
    public void onScanStop() throws Exception {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        ble.stopScan();
    }

    @Override
    public void onConnect(String macCode, final ILinkConnectCallback mLinkConnectCallback) throws Exception {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        if (bluetoothDeviceStore.getDeviceMap().containsKey(macCode)) {
            BleModelDevice bleModelDevice = bluetoothDeviceStore.getDeviceMap().get(macCode);
            this.bleModelDevice=bleModelDevice;
            ble.connect(bleModelDevice, new BleConnectCallback<BleModelDevice>() {
                @Override
                public void onConnectionChanged(BleModelDevice device) {
                    if (device.isConnected()) {
                        try {
                            mLinkConnectCallback.onConnectedReceiver(device);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (device.isDisconnected()) {
                        try {
                            mLinkConnectCallback.onDisConnectedRecevice();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onReady(BleModelDevice device) {
                    super.onReady(device);
                    if (ble != null) {
                        ble.enableNotify(device, true, bleNotifyCallback);
                    }
                }
            });



        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1000) {
                try {
                    onDisConnect(macCode);
                    if (mILinkSmartConfigListener!=null){
                        mILinkSmartConfigListener.onLinkSmartConfigTimeOut(errorCode,errorMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onStartSmartConfig(String macCode, String ssid, String password, int delayMillis,ILinkSmartConfigListener mILinkSmartConfigListener) throws Exception {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        if (delayMillis<60){
            delayMillis=60;
        }
        handler.sendEmptyMessageDelayed(IBluetooth.Constant.DELAYMillis, delayMillis*1000);
        this.ssid = ssid;
        this.password = password;
        this.macCode=macCode;
        this.mILinkSmartConfigListener = mILinkSmartConfigListener;
        if (bluetoothDeviceStore.getDeviceMap().containsKey(macCode)) {
            BleModelDevice bleModelDevice = bluetoothDeviceStore.getDeviceMap().get(macCode);
            this.bleModelDevice=bleModelDevice;
            ble.connect(bleModelDevice, bleModelDeviceCallback);
        }
    }

    BleConnectCallback<BleModelDevice> bleModelDeviceCallback = new BleConnectCallback<BleModelDevice>() {
        @Override
        public void onConnectionChanged(BleModelDevice device) {
            if (device.isConnected()){
            }else if (device.isDisconnected()){
                errorCode= IBluetooth.Constant.ERROR_DISCONNECT_CODE;
                errorMessage= mContext.getResources().getString(R.string.ble_device_error_7010_message);
            }
        }

        @Override
        public void onReady(BleModelDevice device) {
            super.onReady(device);
            if (ble != null) {
                ble.enableNotify(device, true, bleNotifyCallback);
            }
        }

        @Override
        public void onServicesDiscovered(final BleModelDevice modelDevice, BluetoothGatt gatt) {
            if (modelDevice.isConnected()) {
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ble.setMTU(modelDevice.getBleAddress(), 128, new BleMtuCallback<BleModelDevice>() {
                            @Override
                            public void onMtuChanged(BleModelDevice device, int mtu, int status) {
                                super.onMtuChanged(device, mtu, status);
                                if (myGetDeviceInfoThread!=null){
                                    myGetDeviceInfoThread.interrupt();
                                    myGetDeviceInfoThread=null;
                                }
                                myGetDeviceInfoThread=new MyGetDeviceInfoThread();
                                myGetDeviceInfoThread.setBleModelDevice(modelDevice);
                                myGetDeviceInfoThread.start();
                                BluetoothProtocol bluetoothProtocol = new BluetoothProtocolImpl();
                                byte[] bytes = bluetoothProtocol.smartConfigEncode(ssid, password);
                                if (bytes != null) {
                                    ble.writeByUuid(modelDevice, bytes, Ble.options().getUuidService(), Ble.options().getUuidWriteCha(), new BleWriteCallback<BleModelDevice>() {
                                        @Override
                                        public void onWriteSuccess(BleModelDevice device, BluetoothGattCharacteristic characteristic) {
                                            BleLog.e("onWriteSuccess", "onWriteSuccess");
                                        }

                                        @Override
                                        public void onWriteFailed(BleModelDevice device, int failedCode) {
                                            super.onWriteFailed(device, failedCode);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                ).start();
            }
            super.onServicesDiscovered(modelDevice, gatt);

        }

        @Override
        public void onConnectFailed(BleModelDevice device, int errorCode) {
            super.onConnectFailed(device, errorCode);
        }


    };

    BleNotifyCallback<BleModelDevice> bleNotifyCallback = new BleNotifyCallback<BleModelDevice>() {
        @Override
        public void onChanged(BleModelDevice device, BluetoothGattCharacteristic characteristic) {
            try {
                UUID uuid = characteristic.getUuid();
                if (uuid.equals(Ble.options().getUuidReadCha())) {
                    byte[] value = characteristic.getValue();
                    if (value != null) {
                        BluetoothProtocol bluetoothProtocol = new BluetoothProtocolImpl();
                        Map<String, Object> decode = bluetoothProtocol.decode(value);
                        if (decode != null && decode.containsKey("code") && decode.containsKey("value")) {
                            int code = (int) decode.get("code");
                            BleLog.e("decode",decode.toString());
                            if (code == 4) {
                                String valueCode = (String) decode.get("value");
                                BleLog.e("onChanged", "Code:" + code + "value:" + valueCode);
                                String errorMessage = "";
                                if (!TextUtils.isEmpty(valueCode)) {
                                    if (TextUtils.equals("00", valueCode)) {
                                        errorMessage = mContext.getResources().getString(R.string.ble_device_error_7001_message);
                                    } else if (TextUtils.equals("01", valueCode)) {
                                        errorMessage = mContext.getResources().getString(R.string.ble_device_error_7001_message);
                                    } else if (TextUtils.equals("02", valueCode)) {
                                        errorCode= IBluetooth.Constant.ERROR_CONNECT_CODE;
                                        errorMessage = mContext.getResources().getString(R.string.ble_device_error_7002_message);
                                    } else if (TextUtils.equals("03", valueCode)) {
                                        errorCode= IBluetooth.Constant.ERROR_PLATFORM_CODE;
                                        errorMessage = mContext.getResources().getString(R.string.ble_device_error_7003_message);
                                    } else if (TextUtils.equals("0F", valueCode)||TextUtils.equals("0f", valueCode)) {
                                        errorCode= IBluetooth.Constant.ERROR_SUCCESS_CODE;
                                        errorMessage="SUCCESS";
                                        ble.disconnect(device);
                                        handler.removeMessages(IBluetooth.Constant.DELAYMillis);
                                        JSONObject jsonObject=new JSONObject();
                                        jsonObject.put("mac",bleModelDevice.getMacAddress());
                                        jsonObject.put("deviceId",deviceName);
                                        if (mILinkSmartConfigListener!=null){
//                                            Utils.getResult(errorCode,errorMessage,jsonObject);
                                            mILinkSmartConfigListener.onLinkSmartConfigListener(errorCode, errorMessage, jsonObject.toString());
                                        }
                                    } else if (TextUtils.equals("FF", valueCode)||TextUtils.equals("ff", valueCode)) {
                                        ble.disconnect(device);
                                        handler.removeMessages(IBluetooth.Constant.DELAYMillis);
                                        errorMessage = mContext.getResources().getString(R.string.ble_device_error_7255_message);
                                        errorCode= IBluetooth.Constant.ERROR_PASSORD_CODE;
//                                        bluetoothDeviceStore.getDeviceMap().get()
                                        JSONObject jsonObject=new JSONObject();
//                                        jsonObject.put("mac",bleModelDevice.getMacAddress());
//                                        jsonObject.put("deviceId",deviceName);
                                        if (mILinkSmartConfigListener!=null){
//                                            Utils.getResult(errorCode,errorMessage,jsonObject);
                                            mILinkSmartConfigListener.onLinkSmartConfigListener(errorCode, errorMessage, jsonObject.toString());
                                        }
                                    }
                                    BleLog.e("message", errorMessage);
                                }
                            }else if (code==6){
                                String valueHex=(String)decode.get("value");
                                byte [] bytesValue=null;
                                int len=0;
                                if (decode.containsKey("byte")){
                                    bytesValue=(byte[]) decode.get("byte");
                                    len=bytesValue.length;
                                }
                                BleEntityData bleEntityData=new BleEntityData(valueHex,bytesValue,len);
                                if (smartNotifyListener!=null){
                                    smartNotifyListener.onSmartNotifyListener(1,"success",bleEntityData);
                                }
                            }else if (code==8){
                                isOpen=false;
                                if (myGetDeviceInfoThread!=null){
                                    myGetDeviceInfoThread.interrupt();
                                    myGetDeviceInfoThread=null;
                                }
                                byte [] bytesValue=null;
                                int len=0;
                                if (decode.containsKey("byte")){
                                    bytesValue=(byte[]) decode.get("byte");
                                    len=bytesValue.length;
                                    if (bytesValue!=null){
                                        deviceName = new String(bytesValue, "UTF-8");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void sendUartData(String mac, byte[] data, SmartNotifyListener smartNotifyListener) throws Exception {
        this.smartNotifyListener=smartNotifyListener;
        if (ble != null) {
            if (bluetoothDeviceStore.getDeviceMap().containsKey(mac)) {
                BleModelDevice bleModelDevice = bluetoothDeviceStore.getDeviceMap().get(mac);
                if (bleModelDevice != null) {
                    boolean connected = ble.getBleDevice(mac).isConnected();
                    if (connected){
                        BluetoothProtocol bluetoothProtocol = new BluetoothProtocolImpl();
                        byte[] bytes = bluetoothProtocol.hexEncode(data);
                        ble.writeByUuid(bleModelDevice, bytes,
                                Ble.options().getUuidService(),
                                Ble.options().getUuidWriteCha(),
                                BlueISmartImpl.this);
                    }else {
                        if (smartNotifyListener!=null){
                            smartNotifyListener.onSmartNotifyListener(-1, "断开连接", null);
                        }
                    }
                }


            }

        }
    }

    @Override
    public void onDisConnect(String macCode) throws Exception {
        mILinkSmartConfigListener=null;
        handler.removeMessages(IBluetooth.Constant.DELAYMillis);
        isOpen=false;
        if (myGetDeviceInfoThread!=null){
            myGetDeviceInfoThread.interrupt();
            myGetDeviceInfoThread=null;

        }
        smartNotifyListener=null;
        if (ble != null) {
            try {
                if (TextUtils.isEmpty(macCode)) {
                    macCode=this.macCode;
                }
                if (!TextUtils.isEmpty(macCode)) {
                    if (bluetoothDeviceStore.getDeviceMap().containsKey(macCode)) {
                        BleModelDevice bleDevice = bluetoothDeviceStore.getDeviceMap().get(macCode);
                        if (bleDevice != null) {
                            ble.disconnect(bleDevice);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onDestory() throws Exception {
        if (ble != null) {
            ble.released();
        }

    }



    @Override
    public void onWriteSuccess(BleModelDevice device, BluetoothGattCharacteristic characteristic) {

    }


    class  MyGetDeviceInfoThread extends Thread{
        BleModelDevice modelDevice=null;
        public void setBleModelDevice(BleModelDevice bleModelDevice) {
            this.modelDevice = bleModelDevice;
        }
        @Override
        public void run() {
            super.run();
            deviceName="";
            while (isOpen){
                try {
                    Thread.sleep(1000);
                    BluetoothProtocol bluetoothProtocol = new BluetoothProtocolImpl();
                    byte[] bytes = bluetoothProtocol.getDeviceInfo();
                    ble.writeByUuid(modelDevice, bytes,
                            Ble.options().getUuidService(),
                            Ble.options().getUuidWriteCha(),
                            BlueISmartImpl.this);

                } catch (InterruptedException e) {

                }
            }
        }
    }

}
