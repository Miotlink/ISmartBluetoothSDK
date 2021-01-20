package com.miotlink.protocol;

import java.util.Map;

public interface BluetoothProtocol {

    /**
     *配网
     * @param ssid
     * @param password
     * @return
     */
    public byte[] SmartConfigEncode(String ssid,String password);

    public byte[] HeartbeatEncode();

    public byte[] hexEncode(byte[] hex);

    public Map<String,Object> decode(byte [] bytes);
}
