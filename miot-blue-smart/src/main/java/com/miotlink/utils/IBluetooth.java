package com.miotlink.utils;

public class IBluetooth {
    public static String SERVER_UUID="6600";
    public static String SERVER_READ_UUID="6601";
    public static String SERVER_WRITE_UUID="6602";
    public static String SERVER_NOTIFY_UUID="6601";
    public static String FILTER_NAME="MLink";
    public static interface Constant{

        public static final int ERROR_INIT_CODE=7001;
        public static final int ERROR_CONNECT_CODE=7002;
        public static final int ERROR_PLATFORM_CODE=7003;
        public static final int ERROR_SUCCESS_CODE=7015;
        public static final int ERROR_PASSORD_CODE=7255;
        public static final int ERROR_TIME_OUT_CODE=7011;
        public static final int ERROR_DISCONNECT_CODE=7010;

        public static final int DELAYMillis=1000;
    }
}
