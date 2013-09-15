package com.tapioka.android.usbserial;

import android.util.Log;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class CommandWriter {
    private static final String TAG = "CommandWriter";
    SerialInputOutputManager mSerialIoManager;
    
    public CommandWriter(SerialInputOutputManager serialIoManager) {
        mSerialIoManager = serialIoManager;
    }

    public void write(String command, String port, String value){
        Log.e(TAG,command + ":" + port + ":" + value);
        if ("digitalOutput".equals(command)){
            if ("high".equals(value)){
                // test
                byte[] test2 = {(byte)0x01};
                mSerialIoManager.writeAsync(test2);
            } else if ("low".equals(value)){
                
            }
        } else if ("digitalRead".equals(command)){
            
        } else if ("analogWrite".equals(command)){
            
        } else if ("analogRead".equals(command)){
            
        } else {
            Log.e(TAG,"ERROR!!!" + command + ":" + value);
        }
    }
}
