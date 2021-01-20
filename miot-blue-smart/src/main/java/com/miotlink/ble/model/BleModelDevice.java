package com.miotlink.ble.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.miotlink.utils.BlueTools;

public final class BleModelDevice extends BleDevice implements Parcelable {


    /**
     * 蓝牙信号
     */
    private int rssi;

    /**
     * 更新时间
     */
    private long rssiUpdateTime;


    /**
     * 蓝牙广播包数据
     */
    private ScanRecord scanRecord;

    /**
     * 设备品类
     */
    private  int kindId=0;
    /**
     * 设备型号
     */
    private  int modelId=0;

    /**
     * 设备名称
     */
    private  String deviceName="";

    /**
     * 设备MAC地址
     */
    private  String macAddress="";

    /**
     * 标志位
     */
    private int mark=0;

    /**
     * 设备版本号
     */
    private int mVersion=0;

    /**
     * 配网类型 7 妙联app 使用判断区分
     */
    private int mCode=0;

    public int getKindId() {
        return kindId;
    }

    public void setKindId(int kindId) {
        this.kindId = kindId;
    }

    public int getModelId() {
        return modelId;
    }

    public String getDeviceName() {
        return deviceName;
    }


    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getmVersion() {
        return mVersion;
    }

    public void setmVersion(int mVersion) {
        this.mVersion = mVersion;
    }

    public int getmCode() {
        return mCode;
    }

    public void setmCode(int mCode) {
        this.mCode = mCode;
    }


    public BleModelDevice(String address, String name) {
        super(address, name);
    }


    public ScanRecord getScanRecord() {
        return scanRecord;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setScanRecord(ScanRecord scanRecord) {
        if (scanRecord!=null){
            byte[] mScanRecord=scanRecord.getBytes();
            try {
                if (mScanRecord!=null&&mScanRecord.length>20){
                    byte[] bytesMac=null;
                    if (mScanRecord[0]==0x02&&mScanRecord[4]==9){
                        byte [] bytesName=new byte[mScanRecord[3]];
                        System.arraycopy(mScanRecord, 5, bytesName, 0, mScanRecord[3]);
                        deviceName=new String(bytesName,"UTF-8");
                        bytesMac=new byte[mScanRecord.length-(mScanRecord[3]+4)];
                        System.arraycopy(mScanRecord,(mScanRecord[3]+4),bytesMac,0, mScanRecord.length-(mScanRecord[3]+4));
                    }else if (mScanRecord[5]==0x66&&mScanRecord[6]==0x67){
                        bytesMac=mScanRecord;
                    }
                    if (bytesMac!=null){
                        if (bytesMac[5]==0x66&&bytesMac[6]==0x67){
                            if (!TextUtils.isEmpty(getBleName())){
                                deviceName=getBleName();
                            }
                            mCode=7;
                            mVersion=(int)bytesMac[7];
                            mark=(int)bytesMac[8];
                            kindId= BlueTools.BytesToInt32BE(bytesMac,10);
                            modelId=BlueTools.BytesToInt32BE(bytesMac,14);
                            macAddress+=BlueTools.byteToHex(bytesMac[23])+":";
                            macAddress+=BlueTools.byteToHex(bytesMac[22])+":";
                            macAddress+=BlueTools.byteToHex(bytesMac[21])+":";
                            macAddress+=BlueTools.byteToHex(bytesMac[20])+":";
                            macAddress+=BlueTools.byteToHex(bytesMac[19])+":";
                            macAddress+=(BlueTools.byteToHex(bytesMac[18])+"");
                            macAddress=macAddress.toUpperCase();
                            setMacAddress(macAddress);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.scanRecord = scanRecord;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getRssiUpdateTime() {
        return rssiUpdateTime;
    }

    public void setRssiUpdateTime(long rssiUpdateTime) {
        this.rssiUpdateTime = rssiUpdateTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.rssi);
        dest.writeLong(this.rssiUpdateTime);
        dest.writeParcelable(this.scanRecord, flags);
        dest.writeInt(this.kindId);
        dest.writeInt(this.modelId);
        dest.writeString(this.deviceName);
        dest.writeString(this.macAddress);
        dest.writeInt(this.mark);
        dest.writeInt(this.mVersion);
        dest.writeInt(this.mCode);
    }

    protected BleModelDevice(Parcel in) {
        super(in);
        this.rssi = in.readInt();
        this.rssiUpdateTime = in.readLong();
        this.scanRecord = in.readParcelable(ScanRecord.class.getClassLoader());
        this.kindId = in.readInt();
        this.modelId = in.readInt();
        this.deviceName = in.readString();
        this.macAddress = in.readString();
        this.mark = in.readInt();
        this.mVersion = in.readInt();
        this.mCode = in.readInt();
    }

    public static final Creator<BleModelDevice> CREATOR = new Creator<BleModelDevice>() {
        @Override
        public BleModelDevice createFromParcel(Parcel source) {
            return new BleModelDevice(source);
        }

        @Override
        public BleModelDevice[] newArray(int size) {
            return new BleModelDevice[size];
        }
    };
}
