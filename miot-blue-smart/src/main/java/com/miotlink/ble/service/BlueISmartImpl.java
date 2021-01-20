package com.miotlink.ble.service;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.miotlink.ble.Ble;
import com.miotlink.ble.BleLog;
import com.miotlink.ble.callback.BleConnectCallback;
import com.miotlink.ble.callback.BleNotifyCallback;
import com.miotlink.ble.callback.BleScanCallback;
import com.miotlink.ble.callback.BleStatusCallback;
import com.miotlink.ble.callback.BleWriteCallback;
import com.miotlink.ble.listener.ILinkBlueScanCallBack;
import com.miotlink.ble.listener.ILinkConnectCallback;
import com.miotlink.ble.listener.ILinkSmartConfigListener;
import com.miotlink.ble.listener.SmartListener;
import com.miotlink.ble.model.BleFactory;
import com.miotlink.ble.model.BleModelDevice;
import com.miotlink.ble.model.BluetoothDeviceStore;
import com.miotlink.ble.model.ScanRecord;
import com.miotlink.ble.utils.ThreadUtils;
import com.miotlink.ble.utils.Utils;
import com.miotlink.ble.utils.UuidUtils;
import com.miotlink.protocol.BluetoothMessage;
import com.miotlink.protocol.BluetoothProtocol;
import com.miotlink.protocol.BluetoothProtocolImpl;
import com.miotlink.utils.HexUtil;

import java.util.Map;
import java.util.UUID;

public class BlueISmartImpl extends BleWriteCallback<BleModelDevice> implements ISmart {
    private static final String filter_name = "MLink";
    private Ble<BleModelDevice> ble = null;
    private SmartListener mSmartListener = null;
    private Context mContext = null;
    public static boolean Debug = false;
    private BluetoothDeviceStore bluetoothDeviceStore = new BluetoothDeviceStore();


    private String ssid = "";
    private String password = "";
    private ILinkBlueScanCallBack mILinkBlueScanCallBack = null;
    private ILinkSmartConfigListener mILinkSmartConfigListener = null;

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
                .setScanPeriod(5 * 60 * 1000)//设置扫描时长
                .setMaxConnectNum(7)//最大连接数量
                .setUuidService(UUID.fromString(UuidUtils.uuid16To128("6600")))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128("6602")))//设置可写特征的uuid
                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128("6601")))//设置可读特征的uuid （选填）
                .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128("6601")))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
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
    public void onScan(final ILinkBlueScanCallBack mILinkBlueScanCallBack) throws Exception {
        if (ble == null) {
            ble = Ble.getInstance();
        }
        bluetoothDeviceStore.clear();
        this.mILinkBlueScanCallBack = mILinkBlueScanCallBack;
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
                if (!TextUtils.isEmpty(device.getBleName()) && device.getBleName().startsWith(filter_name)
                        || HexUtil.encodeHexStr(scanRecord).contains("6667")) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            device.setScanRecord(ScanRecord.parseFromBytes(scanRecord));
                        }
                        bluetoothDeviceStore.addDevice(device);
                        mILinkBlueScanCallBack.onScanDevice(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };

    @Override
    public void openBluetooth() {


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
            });
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1000) {
                try {
                    if (mILinkSmartConfigListener!=null){
                        mILinkSmartConfigListener.onLinkSmartConfigTimeOut();
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
        handler.sendEmptyMessageDelayed(1000, delayMillis*1000);
        this.ssid = ssid;
        this.password = password;
        this.mILinkSmartConfigListener = mILinkSmartConfigListener;
        if (bluetoothDeviceStore.getDeviceMap().containsKey(macCode)) {
            BleModelDevice bleModelDevice = bluetoothDeviceStore.getDeviceMap().get(macCode);
            bleModelDevice.setAutoConnecting(false);
            ble.connect(bleModelDevice, bleModelDeviceCallback);
        }
    }

    BleConnectCallback<BleModelDevice> bleModelDeviceCallback = new BleConnectCallback<BleModelDevice>() {
        @Override
        public void onConnectionChanged(BleModelDevice device) {

        }

        @Override
        public void onReady(BleModelDevice device) {
            super.onReady(device);
            if (ble != null) {
                ble.enableNotify(device, true, bleNotifyCallback);
            }
        }

        @Override
        public void onServicesDiscovered(BleModelDevice device, BluetoothGatt gatt) {
            super.onServicesDiscovered(device, gatt);
            if (device.isConnected()) {
                BluetoothProtocol bluetoothProtocol = new BluetoothProtocolImpl();
                byte[] bytes = bluetoothProtocol.SmartConfigEncode(ssid, password);
                if (bytes != null) {
                    BleLog.e("onConnectionChanged", HexUtil.encodeHexStr(bytes));
                    ble.writeByUuid(device, bluetoothProtocol.SmartConfigEncode(ssid, password),
                            Ble.options().getUuidService(),
                            Ble.options().getUuidWriteCha(),
                          BlueISmartImpl.this);
                }
            }

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
                            if (code == 4) {
                                String valueCode = (String) decode.get("value");
                                BleLog.e("onChanged", "Code:" + code + "value:" + valueCode);
                                String errorMessage = "";
                                if (!TextUtils.isEmpty(valueCode)) {
                                    if (TextUtils.equals("00", valueCode)) {
                                        errorMessage = "未配网";
                                    } else if (TextUtils.equals("01", valueCode)) {
                                        errorMessage = "未配网";
                                    } else if (TextUtils.equals("03", valueCode)) {
                                        errorMessage = "已连上路由器";
                                    } else if (TextUtils.equals("0F", valueCode)) {
                                        errorMessage = "配网成功";
                                        handler.removeMessages(1000);
                                    } else if (TextUtils.equals("FF", valueCode)) {
                                        handler.removeMessages(1000);
                                        errorMessage = "配网失败";
                                    }
                                    mILinkSmartConfigListener.onLinkSmartConfigListener(Integer.parseInt(valueCode, 16), errorMessage, device.getMacAddress());
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
    public void sendUart(String mac, byte[] data) throws Exception {
        if (ble != null) {
            if (bluetoothDeviceStore.getDeviceMap().containsKey(mac)) {
                BleModelDevice bleModelDevice = bluetoothDeviceStore.getDeviceMap().get(mac);
                if (bleModelDevice != null) {
                    ble.writeByUuid(bleModelDevice, data,
                            Ble.options().getUuidService(),
                            Ble.options().getUuidWriteCha(),
                            BlueISmartImpl.this);
                }
            }

        }
    }

    @Override
    public void onDisConnect(String macCode) throws Exception {
        mILinkSmartConfigListener=null;
        handler.removeMessages(1000);
        if (ble != null) {
            try {
                if (!TextUtils.isEmpty(macCode)) {
                    if (bluetoothDeviceStore.getDeviceMap().containsKey(macCode)) {
                        BleModelDevice bleDevice = bluetoothDeviceStore.getDeviceMap().get(macCode);
                        if (bleDevice != null) {
                            if (bleDevice.isConnecting()) {
                                ble.cancelConnecting(bleDevice);
                            } else if (bleDevice.isConnected()) {
                                ble.disconnect(bleDevice);
                            }
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
}
