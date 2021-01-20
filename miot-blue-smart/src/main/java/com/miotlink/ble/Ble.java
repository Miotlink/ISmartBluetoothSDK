package com.miotlink.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.RequiresApi;

import com.miotlink.ble.callback.BleConnectCallback;
import com.miotlink.ble.callback.BleMtuCallback;
import com.miotlink.ble.callback.BleNotifyCallback;
import com.miotlink.ble.callback.BleReadCallback;
import com.miotlink.ble.callback.BleReadDescCallback;
import com.miotlink.ble.callback.BleReadRssiCallback;
import com.miotlink.ble.callback.BleScanCallback;
import com.miotlink.ble.callback.BleStatusCallback;
import com.miotlink.ble.callback.BleWriteCallback;
import com.miotlink.ble.callback.BleWriteDescCallback;
import com.miotlink.ble.callback.BleWriteEntityCallback;
import com.miotlink.ble.callback.wrapper.BleWrapperCallback;
import com.miotlink.ble.callback.wrapper.BluetoothChangedObserver;
import com.miotlink.ble.exception.BleException;
import com.miotlink.ble.model.BleDevice;
import com.miotlink.ble.model.BleFactory;
import com.miotlink.ble.model.BleModelDevice;
import com.miotlink.ble.model.EntityData;
import com.miotlink.ble.proxy.RequestImpl;
import com.miotlink.ble.proxy.RequestListener;
import com.miotlink.ble.proxy.RequestProxy;
import com.miotlink.ble.queue.RequestTask;
import com.miotlink.ble.queue.WriteQueue;
import com.miotlink.ble.queue.reconnect.DefaultReConnectHandler;
import com.miotlink.ble.request.ConnectRequest;
import com.miotlink.ble.request.DescriptorRequest;
import com.miotlink.ble.request.Rproxy;
import com.miotlink.ble.request.ScanRequest;

import java.util.List;
import java.util.UUID;



/**
 * 这个类对外提供所有的蓝牙操作API
 * Created by jerry on 2016/12/7.
 */
public final class Ble<T extends BleDevice> {

    private final static String TAG = "Ble";
    private static volatile Ble sInstance;
    private static volatile Options options;
    private static final long DEFAULT_WRITE_DELAY = 50L;
    private Context context;
    private RequestListener<T> request;
    private final Object locker = new Object();
    private BleRequestImpl bleRequestImpl;
    //打开蓝牙标志位
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChangedObserver bleObserver;

    public interface InitCallback {
        void success();
        void failed(int failedCode);
    }

    /**
     * Initializes a newly created {@code Ble} object so that it represents
     * a bluetooth management class .  Note that use of this constructor is
     * unnecessary since Can not be externally constructed.
     */
    private Ble(){}

    public static <T extends BleDevice> Ble<T> getInstance(){
        if (sInstance == null) {
            synchronized (Ble.class) {
                if (sInstance == null) {
                    sInstance = new Ble();
                }
            }
        }
        return sInstance;
    }

    /**
     *  蓝牙初始化
     * @param context 上下文对象
     * @return 初始化是否成功
     */
    public void init(Context context, Options options, InitCallback callback) {
        if (context == null){
            throw new BleException("context is null");
        }
        if (this.context != null){
            BleLog.e(TAG, "Ble is Initialized!");
            if (callback != null){
                callback.failed(BleStates.InitAlready);
            }
            return;
        }
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            if (callback != null){
                BleLog.e(TAG, "bluetoothAdapter is not available!");
                callback.failed(BleStates.NotAvailable);
            }
            return;
        }
        if (!isSupportBle(context)){
            if (callback != null){
                BleLog.e(TAG, "not support ble!");
                callback.failed(BleStates.NotSupportBLE);
            }
            return;
        }
        Ble.options = (options == null ? options() : options);
        BleLog.init(Ble.options);
        request = (RequestListener<T>) RequestProxy.newProxy().bindProxy(context, RequestImpl.newRequestImpl());
        bleRequestImpl = BleRequestImpl.getBleRequest();
        bleRequestImpl.initialize(context);
        initBleObserver();
        BleLog.d(TAG, "Ble init success");
        if (callback != null){
            callback.success();
        }
    }

    public static <T extends BleDevice> Ble<T> create(Context context, InitCallback callback){
        return create(context, options(), callback);
    }

    public static <T extends BleDevice> Ble<T> create(Context context, Options options, InitCallback callback) {
        Ble<T> ble = getInstance();
        ble.init(context, options, callback);
        return ble;
    }

    /**
     * 设置全局蓝牙开启、关闭监听
     * @param callback
     */
    public void setBleStatusCallback(BleStatusCallback callback){
        if (bleObserver != null) {
            bleObserver.setBleScanCallbackInner(callback);
        }
    }

    /**
     * 开始扫描
     * @param callback 扫描回调
     */
    public void startScan(BleScanCallback<T> callback){
        request.startScan(callback, options().scanPeriod);
    }

    public void startScan(BleScanCallback<T> callback, long scanPeriod){
        request.startScan(callback, scanPeriod);
    }

    /**
     * 停止扫描
     */
    public void stopScan(){
        request.stopScan();
    }

    /**
     * 连接蓝牙
     *
     * @param device 蓝牙设备对象
     */
    public void connect(T device, BleConnectCallback<T> callback) {
        synchronized (locker) {
            request.connect(device, callback);
        }
    }

    /**
     * 通过mac地址连接设备
     *
     * @param address  mac地址
     * @param callback 连接回调
     */
    public void connect(String address,BleConnectCallback<T> callback){
        synchronized (locker) {
            request.connect(address, callback);
        }
    }

    public void connects(List<T> devices, BleConnectCallback<T> callback) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        request.connect(devices, callback);
    }

    public void cancelConnecting(T device){
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        request.cancelConnecting(device);
    }

    public void cancelConnectings(List<T> devices){
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        request.cancelConnectings(devices);
    }

    /**
     * 动态设置是否自动连接
     * @param device 设备对象
     * @param autoConnect 是否自动连接
     */
    public void autoConnect(T device, boolean autoConnect){
        DefaultReConnectHandler.provideReconnectHandler().resetAutoConnect(device, autoConnect);
    }

    public void cancelAutoConnects(){
        DefaultReConnectHandler.provideReconnectHandler().cancelAutoConnect();
    }

    /**
     * 断开蓝牙  无回调
     *
     * @param device 蓝牙设备对象
     */
    public void disconnect(T device) {
        request.disconnect(device);
    }

    /**
     * 断开蓝牙  有回调
     *
     * @param device 蓝牙设备对象
     */
    public void disconnect(T device, BleConnectCallback<T> callback) {
        request.disconnect(device, callback);
    }

    public void disconnectAll(){
        List<T> connectedDevices = getConnectedDevices();
        if (!connectedDevices.isEmpty()){
            for (T device: connectedDevices) {
                request.disconnect(device);
            }
        }
    }

    /**
     * 连接成功后，开始设置通知
     * @param device 蓝牙设备对象
     * @param callback 通知回调
     * @deprecated Use {@link Ble#enableNotify(T, boolean, BleNotifyCallback)} instead.
     */
    public void startNotify(T device, BleNotifyCallback<T> callback){
        request.notify(device, callback);
    }

    /**
     * 移除通知
     * @param  device 蓝牙设备对象
     * @deprecated Use {@link Ble#enableNotify(T, boolean, BleNotifyCallback)} instead.
     */
    public void cancelNotify(T device, BleNotifyCallback<T> callback){
        request.cancelNotify(device, callback);
    }

    /**
     * 设置通知
     * @param device 蓝牙设备对象
     * @param enable 打开/关闭
     * @param callback 通知回调
     */
    public void enableNotify(T device, boolean enable, BleNotifyCallback<T> callback){
        request.enableNotify(device, enable, callback);
    }

    /**
     * 通过uuid设置指定通知
     * @param device 蓝牙设备对象
     * @param enable 打开/关闭
     * @param serviceUUID 服务uuid
     * @param characteristicUUID 通知特征uuid
     * @param callback 通知回调
     */
    public void enableNotifyByUuid(T device, boolean enable, UUID serviceUUID, UUID characteristicUUID, BleNotifyCallback<T> callback){
        request.enableNotifyByUuid(device, enable, serviceUUID, characteristicUUID, callback);
    }

    /**
     * 读取数据
     * @param device 蓝牙设备对象
     * @param callback 读取结果回调
     */
    public boolean read(T device, BleReadCallback<T> callback){
        return request.read(device, callback);
    }

    /**
     * 写入到指定uuid数据
     * @param device 蓝牙设备对象
     * @param serviceUUID 服务uuid
     * @param characteristicUUID 写入特征uuid
     * @param callback 写入回调
     */
    public boolean readByUuid(T device, UUID serviceUUID, UUID characteristicUUID, BleReadCallback<T> callback){
        return request.readByUuid(device, serviceUUID, characteristicUUID, callback);
    }

    public boolean readDesByUuid(T device, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, BleReadDescCallback<T> callback){
        DescriptorRequest<T> request = Rproxy.getRequest(DescriptorRequest.class);
        return request.readDes(device, serviceUUID, characteristicUUID, descriptorUUID, callback);
    }

    public boolean writeDesByUuid(T device, byte[] data, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, BleWriteDescCallback<T> callback){
        DescriptorRequest<T> request = Rproxy.getRequest(DescriptorRequest.class);
        return request.writeDes(device, data, serviceUUID, characteristicUUID, descriptorUUID, callback);
    }

    /**
     * 读取远程RSSI
     * @param device 蓝牙设备对象
     * @param callback 读取远程RSSI结果回调
     */
    public boolean readRssi(T device, BleReadRssiCallback<T> callback){
        return request.readRssi(device, callback);
    }

    /**
     * 设置MTU
     * @param address 蓝牙设备地址
     * @param mtu mtu大小
     * @return 是否设置成功
     */
    public boolean setMTU(String address, int mtu, BleMtuCallback<T> callback){
        return request.setMtu(address, mtu, callback);
    }

    /**
     * 写入数据
     * @param device 蓝牙设备对象
     * @param data 写入数据字节数组
     * @param callback 写入结果回调
     * @return 写入是否成功
     */
    public boolean write(T device, byte[]data, BleWriteCallback<T> callback){
        return request.write(device, data, callback);
    }

    /**
     * 写入到指定uuid数据
     * @param device 蓝牙设备对象
     * @param data 数据
     * @param serviceUUID 服务uuid
     * @param characteristicUUID 写入特征uuid
     * @param callback 写入回调
     */
    public boolean writeByUuid(T device, byte[]data, UUID serviceUUID, UUID characteristicUUID, BleWriteCallback<T> callback){
        return request.writeByUuid(device, data, serviceUUID, characteristicUUID, callback);
    }

    /**
     *
     * @param delay
     * @param task
     * @deprecated Use {@link Ble#writeQueue(RequestTask task)} instead.
     */
    public void writeQueueDelay(long delay, RequestTask task){
        writeQueue(task);
    }

    public void writeQueue(RequestTask task){
        WriteQueue.getInstance().put(task);
    }

    /**
     * 写入大数据量的数据（分包）
     * @param device 蓝牙设备对象
     * @param data 写入的总字节数组（如整个文件的字节数组）
     * @param packLength 每包需要发送的长度
     * @param delay 每包之间的时间间隔
     * @param callback 发送结果回调
     * @deprecated Use {@link Ble#writeEntity(EntityData, BleWriteEntityCallback)} instead.
     */
    public void writeEntity(T device, final byte[]data, @IntRange(from = 1,to = 20)int packLength, int delay, BleWriteEntityCallback<T> callback){
        request.writeEntity(device, data, packLength, delay, callback);
    }

    /**
     * 写入大数据量数据，需要延迟(分包)
     * 自动模式下写入大数据量数据，无需延迟，根据系统底层返回结果进行连续写入(分包)
     * @param entityData 数据实体
     * @param callback 写入回调
     */
    public void writeEntity(EntityData entityData, BleWriteEntityCallback<T> callback){
        request.writeEntity(entityData, callback);
    }

    public void cancelWriteEntity() {
        request.cancelWriteEntity();
    }

    /**
     * 获取自定义蓝牙服务对象
     * @return 自定义蓝牙服务对象
     */
    public BleRequestImpl getBleRequest() {
        return bleRequestImpl;
    }

    /**
     * 根据蓝牙地址获取蓝牙对象
     * @param address 蓝牙地址
     * @return 对应的蓝牙对象
     */
    public T getBleDevice(String address){
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        return request.getBleDevice(address);
    }

    /**
     * 获取对应蓝牙对象
     * @param device 原生蓝牙对象
     * @return 对应蓝牙对象
     */
    public T getBleDevice(BluetoothDevice device) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if(device != null){
            return request.getBleDevice(device.getAddress());
        }
        return null;
    }

    /**
     * 获取对应锁对象
     */
    public Object getLocker() {
        return locker;
    }

    /**
     * 是否正在扫描
     */
    public boolean isScanning() {
        ScanRequest request = Rproxy.getRequest(ScanRequest.class);
        return request.isScanning();
    }

    /**
     *
     * @return 已经连接的设备集合
     */

    public List<T> getConnectedDevices() {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        return request.getConnectedDevices();
    }

    /**
     * 释放所有资源
     */
    public void released(){
        releaseGatts();
        releaseBleObserver();
        if (isScanning())stopScan();
        bleRequestImpl.release();
        bleRequestImpl = null;
        Rproxy.release();
        context = null;
        BleLog.d(TAG, "AndroidBLE already released");
    }

    /**
     * Release Empty all resources
     */
    private void releaseGatts() {
        BleLog.d(TAG, "BluetoothGatts is released");
        synchronized (locker) {
            List<T> connetedDevices = getConnectedDevices();
            for (T bleDevice : connetedDevices) {
                disconnect(bleDevice);
            }
        }
    }

    private void initBleObserver(){
        if (bleObserver == null){
            bleObserver = new BluetoothChangedObserver(context);
            bleObserver.registerReceiver();
        }
    }

    private void releaseBleObserver() {
        BleLog.d(TAG, "BleObserver is released");
        if (bleObserver != null) {
            bleObserver.unregisterReceiver();
            bleObserver = null;
        }
    }

    /**
     * cancel Callback
     * @param callback (BleScanCallback、BleConnectCallback)
     */
    public void cancelCallback(Object callback){
        if (callback instanceof BleScanCallback){
            ScanRequest request = Rproxy.getRequest(ScanRequest.class);
            request.cancelScanCallback();
        }else if (callback instanceof BleConnectCallback){
            ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
            request.cancelConnectCallback();
        }
    }

    private BluetoothAdapter getBluetoothAdapter(){
        if (bluetoothAdapter == null){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    /**
     *
     * @return 是否支持蓝牙
     */
    public boolean isSupportBle(Context context) {
        return (getBluetoothAdapter() != null
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    /**
     *
     * @return 蓝牙是否打开
     */
    public boolean isBleEnable() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * 打开蓝牙(默认模式--带系统弹出框)
     *
     * @param activity 上下文对象
     */
    public void turnOnBlueTooth(Activity activity) {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!isBleEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * 强制打开蓝牙（不弹出系统弹出框）
     */
    public void turnOnBlueToothNo(){
        if(!isBleEnable()){
            if (bluetoothAdapter != null){
                bluetoothAdapter.enable();
            }
        }
    }

    /**
     * 关闭蓝牙
     */
    public boolean turnOffBlueTooth() {
        if (isBleEnable()){
            return bluetoothAdapter.disable();
        }
        return true;
    }

    /**
     * 清理蓝牙缓存
     * @param address 蓝牙设备地址
     * @return 是否清理成功
     */
    public boolean refreshDeviceCache(String address) {
        if (bleRequestImpl != null) {
            return bleRequestImpl.refreshDeviceCache(address);
        }
        return false;
    }

    public static Options options(){
        if(options == null){
            options = new Options();
        }
        return options;
    }

    public Context getContext(){
        return context;
    }

    /**
     * 蓝牙相关参数配置类
     */
    public static class Options {
        /**
         * 是否打印蓝牙日志
         */
        public boolean logBleEnable = true;
        /**
         * 日志TAG，用于过滤日志信息
         */
        public String logTAG = "AndroidBLE";
        /**
         * 是否抛出蓝牙异常
         */
        public boolean throwBleException = true;
        /**
         * 是否在蓝牙异常断开时自动连接
         */
        public boolean autoConnect = false;
        /**
         * 蓝牙连接超时时长
         */
        public long connectTimeout = 10 * 1000L;
        /**
         * 蓝牙扫描周期时长
         */
        public long scanPeriod = 10 * 1000L;
        /**
         * 服务绑定失败重试次数
         */
        public int serviceBindFailedRetryCount = 3;
        /**
         * 蓝牙连接失败重试次数
         */
        public int connectFailedRetryCount;

        public int maxConnectNum = 7;
        /**
         * 是否过滤扫描设备
         */
        public boolean isIgnoreRepeat = false;

        public ScanFilter scanFilter;
        /**
         * 是否解析广播包  (发送接收广播包时可以打开)
         */
        public boolean isParseScanData = false;
        /**
         * 广播包,厂商id
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public int manufacturerId = 65520; // 0xfff0

        public BleWrapperCallback bleWrapperCallback;

        private BleFactory factory;

        public Options setScanPeriod(long scanPeriod){
            this.scanPeriod = scanPeriod;
            return this;
        }

        public String getLogTAG() {
            return logTAG;
        }

        public Options setLogTAG(String logTAG) {
            this.logTAG = logTAG;
            return this;
        }

        public boolean isLogBleEnable() {
            return logBleEnable;
        }

        public Options setLogBleEnable(boolean logBleEnable) {
            this.logBleEnable = logBleEnable;
            return this;
        }

        public boolean isThrowBleException() {
            return throwBleException;
        }

        public Options setThrowBleException(boolean throwBleException) {
            this.throwBleException = throwBleException;
            return this;
        }

        public boolean isAutoConnect() {
            return autoConnect;
        }

        public Options setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        public long getConnectTimeout() {
            return connectTimeout;
        }

        public Options setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public long getScanPeriod() {
            return scanPeriod;
        }

        public int getServiceBindFailedRetryCount() {
            return serviceBindFailedRetryCount;
        }

        public Options setServiceBindFailedRetryCount(int serviceBindFailedRetryCount) {
            this.serviceBindFailedRetryCount = serviceBindFailedRetryCount;
            return this;
        }

        public int getConnectFailedRetryCount() {
            return connectFailedRetryCount;
        }

        public Options setConnectFailedRetryCount(@IntRange(from = 0, to = 5)int connectFailedRetryCount) {
            this.connectFailedRetryCount = connectFailedRetryCount;
            return this;
        }

        public int getMaxConnectNum() {
            return maxConnectNum;
        }

        public Options setMaxConnectNum(@IntRange(from = 1, to = 7)int maxConnectNum) {
            this.maxConnectNum = maxConnectNum;
            return this;
        }

        public boolean isIgnoreRepeat() {
            return isIgnoreRepeat;
        }

        public Options setIgnoreRepeat(boolean ignoreRepeat) {
            isIgnoreRepeat = ignoreRepeat;
            return this;
        }

        public ScanFilter getScanFilter() {
            return scanFilter;
        }

        public Options setScanFilter(ScanFilter scanFilter) {
            this.scanFilter = scanFilter;
            return this;
        }

        public boolean isParseScanData() {
            return isParseScanData;
        }

        public Options setParseScanData(boolean parseScanData) {
            isParseScanData = parseScanData;
            return this;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public int getManufacturerId() {
            return manufacturerId;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public Options setManufacturerId(int manufacturerId) {
            this.manufacturerId = manufacturerId;
            return this;
        }

        public BleWrapperCallback getBleWrapperCallback() {
            return bleWrapperCallback;
        }

        public Options setBleWrapperCallback(BleWrapperCallback bleWrapperCallback) {
            this.bleWrapperCallback = bleWrapperCallback;
            return this;
        }

        public BleFactory getFactory(){
            if (factory == null){
                factory = new BleFactory() {
                    @Override
                    public BleDevice create(String address, String name) {
                        return super.create(address, name);
                    }
                };
            }
            return factory;
        }

        /**
         * 自定义device时，必须设置factory，不然会造成强制转换异常
         */
        public Options setFactory(BleFactory factory) {
            this.factory = factory;
            return this;
        }

        UUID[] uuid_services_extra = new UUID[]{};
        UUID uuid_service = UUID.fromString("0000fee9-0000-1000-8000-00805f9b34fb");
        UUID uuid_write_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");
        UUID uuid_read_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");
        UUID uuid_notify_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129601");
        UUID uuid_notify_desc = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        UUID uuid_ota_service = UUID.fromString("0000fee8-0000-1000-8000-00805f9b34fb");
        UUID uuid_ota_notify_cha = UUID.fromString("003784cf-f7e3-55b4-6c4c-9fd140100a16");
        UUID uuid_ota_write_cha = UUID.fromString("013784cf-f7e3-55b4-6c4c-9fd140100a16");

        public UUID[] getUuidServicesExtra() {
            return uuid_services_extra;
        }

        public Options setUuidServicesExtra(UUID[] uuid_services_extra) {
            this.uuid_services_extra = uuid_services_extra;
            return this;
        }

        public UUID getUuidService() {
            return uuid_service;
        }

        public Options setUuidService(UUID uuid_service) {
            this.uuid_service = uuid_service;
            return this;
        }

        public UUID getUuidWriteCha() {
            return uuid_write_cha;
        }

        public Options setUuidWriteCha(UUID uuid_write_cha) {
            this.uuid_write_cha = uuid_write_cha;
            return this;
        }

        public UUID getUuidReadCha() {
            return uuid_read_cha;
        }

        public Options setUuidReadCha(UUID uuid_read_cha) {
            this.uuid_read_cha = uuid_read_cha;
            return this;
        }

        public UUID getUuidNotifyCha() {
            return uuid_notify_cha;
        }

        public Options setUuidNotifyCha(UUID uuid_notify_cha) {
            this.uuid_notify_cha = uuid_notify_cha;
            return this;
        }

        public UUID getUuidNotifyDesc() {
            return uuid_notify_desc;
        }

        public Options setUuidNotifyDesc(UUID uuid_notify_desc) {
            this.uuid_notify_desc = uuid_notify_desc;
            return this;
        }

        public UUID getUuidOtaService() {
            return uuid_ota_service;
        }

        public Options setUuidOtaService(UUID uuid_ota_service) {
            this.uuid_ota_service = uuid_ota_service;
            return this;
        }

        public UUID getUuidOtaNotifyCha() {
            return uuid_ota_notify_cha;
        }

        public Options setUuidOtaNotifyCha(UUID uuid_ota_notify_cha) {
            this.uuid_ota_notify_cha = uuid_ota_notify_cha;
            return this;
        }

        public UUID getUuidOtaWriteCha() {
            return uuid_ota_write_cha;
        }

        public Options setUuidOtaWriteCha(UUID uuid_ota_write_cha) {
            this.uuid_ota_write_cha = uuid_ota_write_cha;
            return this;
        }

        public <T extends BleDevice> Ble<T> create(Context context){
            return create(context, null);
        }

        public <T extends BleDevice> Ble<T> create(Context context, InitCallback callback){
            return Ble.create(context, callback);
        }

    }

}
