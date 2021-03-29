package com.miotlink.ble.model;

import java.util.Arrays;

public class BleEntityData {
    private String uartdata="";
    private byte [] bytes=null;
    private int len=0;

    public BleEntityData(String uartdata, byte[] bytes, int len) {
        this.uartdata = uartdata;
        this.bytes = bytes;
        this.len = len;
    }

    public String getUartdata() {
        return uartdata;
    }

    public void setUartdata(String uartdata) {
        this.uartdata = uartdata;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    @Override
    public String toString() {
        return "BleEntity{" +
                "uartdata='" + uartdata + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                ", len=" + len +
                '}';
    }
}
