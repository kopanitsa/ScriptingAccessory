/* Copyright 2011 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: http://code.google.com/p/usb-serial-for-android/
 */

package com.tapioka.android.usbserial;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.facade.ActivityResultFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.tapioka.android.R;
import com.tapioka.android.interpreter.ScriptApplication;
import com.tapioka.android.interpreter.ScriptService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monitors a single {@link UsbSerialDriver} instance, showing all data
 * received.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialConsoleActivity extends Activity {

    private final String TAG = SerialConsoleActivity.class.getSimpleName();
    private final String STRING_EOS = "_EOS_";

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialDriver)}.
     *
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialDriver sDriver = null;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;
    private CommandWriter mCommandWriter;

    private SerialIoBroadcastReceiver mTestReceiver;
    private IntentFilter mIntentFilter;
    
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, HexDump.dumpHexString(data));
                    SerialConsoleActivity.this.updateReceivedData(data);
                    if (!SerialConsoleActivity.this.saveReceivedData(data)) {
                    	SerialConsoleActivity.this.startService();
                    }
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        deleteFile("script.py"); // Delete script.py
        Button onButton = (Button) findViewById(R.id.on_btn);
        onButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] test = {(byte)0xFF}; // test send data 
                if (mSerialIoManager != null) {
                    Log.d(TAG, "on click ON");
                    mSerialIoManager.writeAsync(test);
                }
                deleteFile("script.py"); // Delete script.py
            }
        });
        Button offButton = (Button) findViewById(R.id.off_btn);
        offButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] test = {(byte)0x02}; // test send data 
                if (mSerialIoManager != null) {
                    Log.d(TAG, "on click OFF");
                    mSerialIoManager.writeAsync(test);
                }
                deleteFile("script.py"); // Delete script.py
            }
        });
        
        mTestReceiver = new SerialIoBroadcastReceiver();
        mIntentFilter = new IntentFilter("com.tapioka.android.usbserial.IO");
        registerReceiver(mTestReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
// TODO
//        stopIoManager();
//        if (sDriver != null) {
//            try {
//                sDriver.close();
//            } catch (IOException e) {
//                // Ignore.
//            }
//            sDriver = null;
//        }
        unregisterReceiver(mTestReceiver);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed, sDriver=" + sDriver);
        if (sDriver == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            try {
                sDriver.open();
                sDriver.setParameters(115200, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sDriver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sDriver = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sDriver.getClass().getSimpleName());
        }
        onDeviceStateChange();
        mCommandWriter = new CommandWriter(mSerialIoManager);
        
        // send start command to Arduino.
        // When Arduino get the command (serial_test2.ino), Arduino start to send script.
        byte[] start = {CommandWriter.START_TO_SEND_SCRIPT};
        mSerialIoManager.writeAsync(start);
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sDriver != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sDriver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    private boolean saveReceivedData(byte[] data) {
    	boolean saving = false;
        final String stringData = new String(data);

        if (stringData.indexOf(STRING_EOS) != -1) {
        	Log.d(TAG, "Receive EOS");
        	saving = false;
        } else {
	    	PrintWriter pw = null;
	    	saving = true;
	        try {
	            pw = new PrintWriter(
	            		new OutputStreamWriter(
	            				openFileOutput("script.py",MODE_APPEND)));
	
	            pw.print(stringData);
	            pw.close();
	        } catch(IOException e) {
	            e.printStackTrace();
	        }
	        finally {
	        	if (pw != null) {
	                pw.close();
	            }
	        }
        }
        return saving;
    }

    private void startService(){
        if (Constants.ACTION_LAUNCH_SCRIPT_FOR_RESULT.equals(getIntent().getAction())) {
            setTheme(android.R.style.Theme_Dialog);
            setContentView(R.layout.dialog);
            ServiceConnection connection = new ServiceConnection() {
              @Override
              public void onServiceConnected(ComponentName name, IBinder service) {
                ScriptService scriptService = ((ScriptService.LocalBinder) service).getService();
                try {
                  RpcReceiverManager manager = scriptService.getRpcReceiverManager();
                  ActivityResultFacade resultFacade = manager.getReceiver(ActivityResultFacade.class);
                  resultFacade.setActivity(SerialConsoleActivity.this);
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public void onServiceDisconnected(ComponentName name) {
                // Ignore.
              }
            };
            bindService(new Intent(this, ScriptService.class), connection, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, ScriptService.class));
        } else {
            ScriptApplication application = (ScriptApplication) getApplication();
            if (application.readyToStart()) {
                ServiceConnection connection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        ScriptService scriptService = ((ScriptService.LocalBinder) service).getService();
                        scriptService.setCommandWriter(mCommandWriter);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        // Ignore.
                    }
                };
                bindService(new Intent(this, ScriptService.class), connection, Context.BIND_AUTO_CREATE);
                startService(new Intent(this, ScriptService.class));
            }
            finish();
        }
    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     * @param driver
     */
    public static void show(Context context, UsbSerialDriver driver) {
        sDriver = driver;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public class SerialIoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                String command = extras.getString("command");
                String port = extras.getString("port");
                String value = extras.getString("value");
                Log.e(TAG, command + "/" + port + "/" + value);
                Toast.makeText(context, command + "/" + port + "/" + value, Toast.LENGTH_LONG).show();
                mCommandWriter.write(command,port,value);
            }
        }
    }    
}
