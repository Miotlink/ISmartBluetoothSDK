package com.miotlink.protocol;

import java.util.Map;

public interface BluetoothProtocol {

    /**
     *配网
     * @param ssid
     * @param password
     * @return
     */
    public byte[] smartConfigEncode(String ssid, String password);

    public byte[] HeartbeatEncode();

    public byte[] bleSmartConfig();

    public byte[] getDeviceInfo();
    public byte[] hexEncode(byte[] hex);

    public Map<String,Object> decode(byte[] bytes);
}
