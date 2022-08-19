package com.miotlink.ble.model;



import android.text.TextUtils;

import com.miotlink.utils.HexUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothDeviceStore {

    private final Map<String, BleModelDevice> mDeviceMap;

    public BluetoothDeviceStore() {
        mDeviceMap = new HashMap<>();
    }

    public void addDevice(BleModelDevice device) {
        if (device == null) {
            return;
        }
        if (!TextUtils.isEmpty(device.getMacAddress())){
            if (!mDeviceMap.containsKey(device.getMacAddress())) {
                mDeviceMap.put(device.getMacAddress(), device);
            }
        }else if (!TextUtils.isEmpty(device.getBleName())
                &&device.getBleName().startsWith("Hi-Huawei-Mars")||
                HexUtil.encodeHexStr(device.getScanRecord().getBytes()==null?new byte[0]:device.getScanRecord().getBytes()).contains("6667")){
            if (!mDeviceMap.containsKey(device.getBleAddress())) {
                mDeviceMap.put(device.getBleAddress(), device);
            }
        }

    }

    public void removeDevice(BleModelDevice device) {
        if (device == null) {
            return;
        }
        if (mDeviceMap.containsKey(device.getBleAddress())) {
            mDeviceMap.remove(device.getBleAddress());
        }
    }

    public void clear() {
        mDeviceMap.clear();
    }

    public Map<String, BleModelDevice> getDeviceMap() {
        return mDeviceMap;
    }

    public List<BleModelDevice> getDeviceList() {
        final List<BleModelDevice> methodResult = new ArrayList<>(mDeviceMap.values());
        return methodResult;
    }

    @Override
    public String toString() {
        return "BluetoothLeDeviceStore{" +
                "DeviceList=" + getDeviceList() +
                '}';
    }
}
