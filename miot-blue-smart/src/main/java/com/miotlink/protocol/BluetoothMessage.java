package com.miotlink.protocol;

import com.miotlink.utils.BlueTools;
import com.miotlink.utils.BluetoothConsts;

import java.util.ArrayList;
import java.util.List;



public class BluetoothMessage {


    private byte[] bytes=new byte[64];

    private byte[] start_head={BluetoothConsts.START_HEAD_1,BluetoothConsts.START_HEAD_2};
    private byte[] start_end={BluetoothConsts.END_1,BluetoothConsts.END_2};

    private int length=2;

    private  BlueMessageBody blueMessageBody=null;

    private byte mBuffer[]=null;


    public BluetoothMessage(){
        System.arraycopy(start_head,0,bytes, 0, start_head.length);
    }

   public BlueMessageBody getBlueMessageBody(int code,int paramsNum){
         blueMessageBody=new BlueMessageBody(code,paramsNum);
       return blueMessageBody;
    }

    public void encode(){
        if (blueMessageBody!=null){
            blueMessageBody.message();
            byte[] buff = blueMessageBody.getBytes();
            bytes[length]=(byte)(blueMessageBody.getLength()+length);
            length+=1;
            int time=(int) (System.currentTimeMillis()%65536);
            bytes= BlueTools.Int16ToBytesBE(time,bytes,length);
            length+=2;
            System.arraycopy(buff,0,bytes, length, blueMessageBody.getLength());
            length+=blueMessageBody.getLength();
            System.arraycopy(start_end,0,bytes,length,start_end.length);
            length+=2;
            mBuffer=new byte[length];
            System.arraycopy(bytes,0,mBuffer, 0, length);
        }
    }


    public byte[] getmBytes() {
        return mBuffer;
    }

    public int getLength() {
        return length;
    }


    public BlueMessageBody decode(byte[] bytes){
        try {
            this.bytes=bytes;
            if (bytes==null||bytes[0]!=BluetoothConsts.START_HEAD_1&&bytes[1]!=BluetoothConsts.START_HEAD_2){
                return null;
            }
            if (bytes.length<8){
                return null;
            }
            this.length=bytes[2];
            blueMessageBody=new BlueMessageBody();
            List<Object> propertys = blueMessageBody.getPropertys(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blueMessageBody;
    }




    public static class BlueMessageBody{

        private int code=0;
        /**
         * 功能
         */
        private int funcation=0;
        private byte[] bytes=new byte[128];
        private int length=0;
        private int paramNum;

        public BlueMessageBody(){

        }

        public BlueMessageBody(int code,int paramNum){
            this.code=code;
            this.paramNum=paramNum;
            bytes[length]=(byte)code;
            bytes[length+1]=(byte)paramNum;
            length+=2;
        }
        public int getCode() {
            return code;
        }

        public int getLength() {
            return length;
        }


        public void addPropertys(int len,Object o){
            bytes[length]=(byte)len;
            length+=1;
            if (len==4&&o instanceof Integer){
               bytes= BlueTools.Int32ToBytesLE((int)o,bytes,length);
            }else if (len>0&& o instanceof String){
                byte[] bytesParams = ((String) o).getBytes();
                System.arraycopy(bytesParams,0,bytes, length, bytesParams.length);
            }else if (len>0 && o instanceof Byte){
                byte [] bs=(byte[]) o;
                System.arraycopy(bs,0,bytes, length, bs.length);
            }
            this.length+=len;

        }

        public void message(){
            byte[] bytesMsg=new byte[length];
            System.arraycopy(bytes, 0, bytesMsg, 0, length);
            String crcValue = CRC16Utils.getCRCValue(bytesMsg);
            bytes[length+1]=(byte)CRC16Utils.getCrcMaxLen(crcValue);
            bytes[length]=(byte)CRC16Utils.getCrcMinLen(crcValue);
            length+=2;

        }


        public List<Object> getPropertys(byte [] bytes)throws Exception{
            this.bytes=bytes;

            if (bytes==null){
                return null;
            }
            if (bytes.length<8){
                return null;
            }
            List<Object> list=new ArrayList<>();
            this.code=bytes[5];
            this.paramNum=bytes[6];
            if (paramNum>0){
                int startPos=7;
                for (int i=0;i<paramNum;i++){
                    byte [] bytes1=new byte[bytes[startPos]];
                    System.arraycopy(bytes,startPos+1,bytes1,0,bytes[startPos]);
                    list.add(bytes1);
                    startPos+=bytes[startPos]+1;
                }
            }
            return list;
        }



        public byte[] getBytes() {
            return bytes;
        }
    }


}
