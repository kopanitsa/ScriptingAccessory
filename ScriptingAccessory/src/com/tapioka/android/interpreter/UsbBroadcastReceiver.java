package com.tapioka.android.interpreter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbBroadcastReceiver extends BroadcastReceiver {

    ScriptService mService;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();   
        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // execute script 
            if (mService != null){
                mService.executeDisconnectedScript();
            }
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);  
            if (device != null) {  
                // call your method that cleans up and closes communication with the device  
                // TODO finish services
            }  
        }  
    }

    public void setService(ScriptService scriptService) {
        mService = scriptService;
    }

    
}
