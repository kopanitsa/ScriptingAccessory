package com.tapioka.android.usbserial;

import android.util.Log;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class CommandWriter {
    private static final byte DIGITAL_WRITE = (byte)0x01;
    private static final byte DIGITAL_READ  = (byte)0x02;
    private static final byte ANALOG_WRITE  = (byte)0x03;
    private static final byte ANALOG_READ   = (byte)0x04;
    private static final byte HIGH = (byte)0x01;
    private static final byte LOW = (byte)0x00;

    private static final String TAG = "CommandWriter";
    SerialInputOutputManager mSerialIoManager;

    public CommandWriter(SerialInputOutputManager serialIoManager) {
        mSerialIoManager = serialIoManager;
    }

    public void write(String command, String port, String value){
        Log.e(TAG,command + ":" + port + ":" + value);
        // need consider 
        if ("digitalWrite".equals(command)){
            if ("high".equals(value)){
                byte[] b = {DIGITAL_WRITE, HIGH};
                mSerialIoManager.writeAsync(b);
            } else if ("low".equals(value)){
                byte[] b = {DIGITAL_WRITE, LOW};
                mSerialIoManager.writeAsync(b);
            }
        } else if ("digitalRead".equals(command)){
            // TODO
        } else if ("analogWrite".equals(command)){
            byte[] b = {ANALOG_WRITE, Byte.parseByte(value)};
            mSerialIoManager.writeAsync(b);
        } else if ("analogRead".equals(command)){
            // TODO
        } else {
            Log.e(TAG,"ERROR!!!" + command + ":" + port + ":" + value);
        }
    }

    // TODO
    public void writeRaw(String command){
//        stringtobyte
//        mSerialIoManager.writeAsync(command);
    }


}
