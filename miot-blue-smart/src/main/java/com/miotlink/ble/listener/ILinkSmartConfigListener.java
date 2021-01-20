package com.miotlink.ble.listener;

public interface ILinkSmartConfigListener {

    /**
     * 1 代表未配网  2 代表配网中 3 代表已连上路由器  15 代表 配网成功  255 代表配网失败
     * @param error
     * @param errorMessage
     * @param data
     * @throws Exception
     */
    public void onLinkSmartConfigListener(int error,String errorMessage,String data)throws Exception;


    public void onLinkSmartConfigTimeOut()throws Exception;
}
