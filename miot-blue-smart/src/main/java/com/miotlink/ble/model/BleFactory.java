package com.miotlink.ble.model;

public abstract class BleFactory<T extends BleDevice> {

    public T create(String address, String name) {
        return (T) new BleDevice(address, name);
    }

}
