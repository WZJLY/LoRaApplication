package com.example.loraapplication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android_serialport_api.SerialPort;

//new serial port
public class SerialPortInterface extends AppCompatActivity {
    FileOutputStream mOutputStream = null;
    FileInputStream mInputStream = null;
    SerialPort sp;
    Context context;


    public SerialPortInterface(Context context, String serialPortID) {
        this.context = context;
        try {
            sp = new SerialPort(new File(serialPortID), 9600, 0);
            mOutputStream = (FileOutputStream) sp.getOutputStream();
            mInputStream = (FileInputStream) sp.getInputStream();
        } catch (SecurityException e) {
            Toast.makeText(this.context,"锁控板连接异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this.context,"锁控板连接异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 计算产生校验码
     * @param data 需要校验的数据
     * @return 校验码
     */
    public static String Make_CRC(byte[] data, int start, int num) {
        byte[] buf = new byte[num];// 存储需要产生校验码的数据
        for (int i = 0; i < num; i++) {
            buf[i] = data[start+i];
        }
        int len = num;
        int crc = 0xFFFF;//16位
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        String c = Integer.toHexString(crc);
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }
        return c;
    }
    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /*
    * 输入字符串w 返回lrc校验部分
    * @param  w=需要传输的信息  ID是地址
    * @return finaldata
    */
    public String Lrc(String w) {
        char[] m = w.toCharArray();
        int x = 0;
        int length = m.length;
        int[] lrcdata = new int[length];
        for (int i = 0; i < length; i++) {
            if (m[i] >= 'A')
                lrcdata[i] = m[i] - 'A' + 10;
            else
                lrcdata[i] = m[i] - '0';
        }
        for (int i = 0; i < length / 2; i++) {
            x += (lrcdata[2 * i] * 16 + lrcdata[2 * i + 1]);
        }
        x = x % 256;
        x = 256 - x;
        String finaldata = Integer.toHexString(x % 256).toUpperCase();
        return finaldata;
    }




    /**
     * 接收数据
     * @param
     * @return
     */
    public String readTHData() {
        String readDatas = null;
        String Error ="" ;
        String lrc;
        byte[] buffer2 ;
        byte[] buffer3 = new byte[1];
        byte[] buffer4 = new byte[1];
        byte[] buffer5 = new byte[1];
        byte[] buffer6 = new byte[1];
        if(mInputStream ==  null){
//            Toast.makeText(this.context,"无法读取串口数据", Toast.LENGTH_SHORT).show();
            return Error;
        }

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(50);
                if (mInputStream.available() > 0) {
                    if (mInputStream != null) {
                        byte[] buffer = new byte[2048];
                        int size = mInputStream.read(buffer);
                        if(size>5)
                        {
                            buffer2=new byte[6];
                            for (int j =0;j<6;j++)
                            {
                                buffer2[j]=buffer[j];
                            }
                            byte[] buffer7 = new byte[2];
                            byte[] buffer8 = new byte[2];
                            buffer3[0]=buffer2[0];
                            buffer4[0]=buffer2[1];
                            buffer5[0]=buffer2[3];
                            buffer6[0]=buffer2[4];
                            System.arraycopy(buffer3,0,buffer7,0,1);
                            System.arraycopy(buffer4,0,buffer7,1,1);
                            System.arraycopy(buffer5,0,buffer8,0,1);
                            System.arraycopy(buffer6,0,buffer8,1,1);
//                            Log.d("wzj",bytesToHexFun1(buffer5));
                         double template = Integer.parseInt(bytesToHexFun1(buffer7),16)*175.0/65535.0-45.0;
                         double rh = Integer.parseInt(bytesToHexFun1(buffer8),16)*100.0/65535.0;
                            String.format("%.1f", rh) ;
                            String.format("%.1f", template);
                          return  "123";



                        }

                    }
                }
            }catch(Exception e){
                return null;
            }
        }
        return null;
    }


    public  String bytesToHexFun1(byte[] bytes) {
        // 一个byte为8位，可用两个十六进制位标识
          final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for(byte b : bytes) { // 使用除与取余进行转换
            if(b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }

            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }

        return new String(buf);
    }

}