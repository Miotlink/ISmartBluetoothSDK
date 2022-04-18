package com.miotlink.protocol;

import android.text.TextUtils;

import com.miotlink.ble.utils.ByteUtils;
import com.miotlink.utils.HexUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

 class BluetoothProtocolImpl implements BluetoothProtocol {


    @Override
    public byte[] smartConfigEncode(String ssid, String password) {
        BluetoothMessage bluetoothMessage=new BluetoothMessage();
        BluetoothMessage.BlueMessageBody blueMessageBody = bluetoothMessage.getBlueMessageBody(3, 2);
        if (blueMessageBody!=null){
            if (!TextUtils.isEmpty(ssid)){
                blueMessageBody.addPropertys(ssid.getBytes().length, ssid);
            }
            if (!TextUtils.isEmpty(password)){
                blueMessageBody.addPropertys(password.getBytes().length, password);
            }
            bluetoothMessage.encode();
            return bluetoothMessage.getmBytes();
        }

        return new byte[0];
    }


     @Override
    public byte[] HeartbeatEncode() {
        BluetoothMessage bluetoothMessage=new BluetoothMessage();
        BluetoothMessage.BlueMessageBody blueMessageBody = bluetoothMessage.getBlueMessageBody(1, 1);
        if (blueMessageBody!=null){
            blueMessageBody.addPropertys(4, (int)System.currentTimeMillis());
            bluetoothMessage.encode();

            return blueMessageBody.getBytes();
        }
        return new byte[0];
    }

     @Override
     public byte[] bleSmartConfig() {
         BluetoothMessage bluetoothMessage=new BluetoothMessage();
         BluetoothMessage.BlueMessageBody blueMessageBody = bluetoothMessage.getBlueMessageBody(7, 1);
         if (blueMessageBody!=null){
             blueMessageBody.addPropertys(1, new byte[]{0x01});
             bluetoothMessage.encode();
             return bluetoothMessage.getmBytes();
         }
         return new byte[0];
     }

     @Override
     public byte[] getDeviceInfo() {
         BluetoothMessage bluetoothMessage=new BluetoothMessage();
         BluetoothMessage.BlueMessageBody blueMessageBody = bluetoothMessage.getBlueMessageBody(7, 1);
         blueMessageBody.addPropertys(1, 0x01);
         bluetoothMessage.encode();
         return bluetoothMessage.getmBytes();
     }

     @Override
    public byte[] hexEncode(byte[] hex) {
        BluetoothMessage bluetoothMessage=new BluetoothMessage();
        BluetoothMessage.BlueMessageBody blueMessageBody = bluetoothMessage.getBlueMessageBody(2, 1);
        blueMessageBody.addPropertys(hex.length, hex);
        bluetoothMessage.encode();
        return blueMessageBody.getBytes();
    }

    @Override
    public Map<String, Object> decode(byte[] bytes) {
        Map<String, Object> value=new HashMap<>();
        BluetoothMessage bluetoothMessage=new BluetoothMessage();
        BluetoothMessage.BlueMessageBody decode = bluetoothMessage.decode(bytes);
        if (decode!=null){
            try {
                List<Object> propertys = decode.getPropertys(bytes);
                if (propertys!=null){
                    value.put("code", decode.getCode());
                    byte[] bytes1=(byte[]) propertys.get(0);
                    switch (decode.getCode()){
                        case 2:
                            value.put("value",  HexUtil.encodeHexStr(bytes1));
                            value.put("byte",  bytes1);
                            break;
                        case 4:
                            value.put("value",  HexUtil.encodeHexStr(bytes1));
                            value.put("byte",  bytes1);
                            break;
                        case 6:
                            value.put("value", HexUtil.encodeHexStr(bytes1));
                            value.put("byte",  bytes1);
                            break;
                        case 8:
                            value.put("value", HexUtil.encodeHexStr(bytes1));
                            value.put("byte",  bytes1);
                            break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return value;
    }
}
