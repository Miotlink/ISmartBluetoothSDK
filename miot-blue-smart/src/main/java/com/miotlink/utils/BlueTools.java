package com.miotlink.utils;

public class BlueTools {

    static public int BytesToInt16LE(byte[] buff, int startPos) {
        short a = (short) ((buff[startPos + 1] & 0xff) << 8);
        short c = (short) (buff[startPos] & 0xff);
        return (short) (a + c);
    }

    static public int BytesToInt16BE(byte[] buff, int startPos) {
        short a = (short) ((buff[startPos] & 0xff) << 8);
        short c = (short) (buff[startPos + 1] & 0xff);
        return (short) (a + c);
    }

    static int toInt(byte[] b, int offset) {
        int value= 0;
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    public static String byteToHex(byte b){
        String hex = Integer.toHexString(b & 0xFF);
        if(hex.length() < 2){
            hex = "0" + hex;
        }
        return hex;
    }
    // 256*256*256 = 16777216, 256*256=65536
    static public void Int16ToBytesLE(int n, byte[] buff, int startPos) {
        int m = n % 65536;
        buff[startPos + 1] = (byte) (m / 256);
        buff[startPos] = (byte) (m % 256);
    }

    static public byte[] Int16ToBytesBE(int n, byte[] buff, int startPos) {
        int m = n % 65536;
        buff[startPos] = (byte) (m / 256);
        buff[startPos + 1] = (byte) (m % 256);

        return buff;
    }

    static public int  BytesToInt32BE(byte[] buff, int startPos) {
        int a = (int) ((buff[startPos] & 0xff) << 24);
        int b = (int) ((buff[startPos + 1] & 0xff) << 16);
        int c = (int) ((buff[startPos + 2] & 0xff) << 8);
        int d = (int) (buff[startPos + 3] & 0xff);
        return (int) (a + b + c + d);
    }

    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    static public void Int32ToBytesBE(int n, byte[] buff, int startPos) {
        buff[startPos] = (byte) (n / 16777216);// 21
        buff[startPos + 1] = (byte) ((n % 16777216) / 65536);// 1
        buff[startPos + 2] = (byte) ((n % 65536) / 256);// 0
        buff[startPos + 3] = (byte) (n % 256);// 0
    }

    static public void LongToBytesBE(long n, byte[] buff, int startPos) {
        buff[startPos] = (byte) (n / 16777216);// 21
        buff[startPos + 1] = (byte) ((n % 16777216) / 65536);// 1
        buff[startPos + 2] = (byte) ((n % 65536) / 256);// 0
        buff[startPos + 3] = (byte) (n % 256);// 0
    }

    static public int BytesToInt32LE(byte[] buff, int startPos) {
        int a = (int) ((buff[startPos + 3] & 0xff) << 24);
        int b = (int) ((buff[startPos + 2] & 0xff) << 16);
        int c = (int) ((buff[startPos + 1] & 0xff) << 8);
        int d = (int) (buff[startPos] & 0xff);
        return (int) (a + b + c + d);
    }

    static public byte[] Int32ToBytesLE(int n, byte[] buff, int startPos) {
        buff[startPos + 3] = (byte) (n / 16777216);
        buff[startPos + 2] = (byte) ((n % 16777216) / 65536);
        buff[startPos + 1] = (byte) ((n % 65536) / 256);
        buff[startPos] = (byte) (n % 256);
        return buff;
    }

    static public String LongIp2String(long nIp) {
        // Log.d(tag,"LongIp2String: "+(nIp/16777216)+","+((nIp%16777216)/65536));
        String sIp = ((nIp / 16777216) & 0xFF) + "."
                + (((nIp % 16777216) / 65536) & 0xFF) + "."
                + (((nIp % 65536) / 256) & 0xFF) + "." + ((nIp % 256) & 0xFF);
        return sIp;
    }

    static public long String2LongIp(String ip) {
        // Log.d(tag,"LongIp2String: "+(nIp/16777216)+","+((nIp%16777216)/65536));
        String[] ns = ip.split("\\.");
        if ( ns.length != 4 ){
            return 0l;
        }

        return Long.parseLong(ns[0])*16777216
                +Long.parseLong(ns[1])*65536
                +Long.parseLong(ns[2])*256
                +Long.parseLong(ns[3]);
    }

    static public String Bin2HexString(byte[] buff, int startPos, int length) {
        String sRet = "{";
        int endPos = (startPos + length) > buff.length ? (buff.length - startPos)
                : length;
        if (endPos > 10240)
            return "{ length > 10240!!! }";
        int count = 0;
        for (int i = startPos; i < endPos; i++) {
            String hex = Integer.toHexString(buff[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sRet += hex;
            if ((++count) % 8 == 0)
                sRet += ".";
            else if (count % 4 == 0)
                sRet += " ";
        }

        return sRet + "}";
    }

}
