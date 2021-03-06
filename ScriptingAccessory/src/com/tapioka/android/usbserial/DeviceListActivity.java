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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.facade.ActivityResultFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.tapioka.android.R;
import com.tapioka.android.interpreter.ScriptApplication;
import com.tapioka.android.interpreter.ScriptService;
import com.tapioka.android.interpreter.ScriptService.LocalBinder;
import com.tapioka.android.usbserial.DeviceListActivity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a {@link ListView} of available USB devices.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class DeviceListActivity extends Activity {

    private final String TAG = DeviceListActivity.class.getSimpleName();
    private final boolean DEBUG_UI = false;

    private UsbManager mUsbManager;
    private ListView mListView;
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;
    
    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private static final int ARDUINO_MICRO_VID = 32823;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    /** Simple container for a UsbDevice and its driver. */
    private static class DeviceEntry {
        public UsbDevice device;
        public UsbSerialDriver driver;

        DeviceEntry(UsbDevice device, UsbSerialDriver driver) {
            this.device = device;
            this.driver = driver;
        }
    }

    private List<DeviceEntry> mEntries = new ArrayList<DeviceEntry>();
    private ArrayAdapter<DeviceEntry> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // DEBUG_UI
        //mListView = (ListView) findViewById(R.id.deviceList);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBarTitle = (TextView) findViewById(R.id.progressBarTitle);
        View.OnTouchListener startButtonListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    startService();
                    return true;
                }
                return false;
            }
        };
        // DEBUG_UI
        //findViewById(R.id.start_button).setOnTouchListener(startButtonListener);

        mAdapter = new ArrayAdapter<DeviceEntry>(this, android.R.layout.simple_expandable_list_item_2, mEntries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null){
                    final LayoutInflater inflater =
                            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final DeviceEntry entry = mEntries.get(position);
                final String title = String.format("Vendor %s Product %s",
                        HexDump.toHexString((short) entry.device.getVendorId()),
                        HexDump.toHexString((short) entry.device.getProductId()));
                row.getText1().setText(title);

                final String subtitle = entry.driver != null ?
                        entry.driver.getClass().getSimpleName() : "No Driver";
                row.getText2().setText(subtitle);

                return row;
            }

        };
        if (DEBUG_UI){
            mListView.setAdapter(mAdapter);
    
            mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "Pressed item " + position);
                    if (position >= mEntries.size()) {
                        Log.w(TAG, "Illegal position.");
                        return;
                    }
    
                    final DeviceEntry entry = mEntries.get(position);
                    final UsbSerialDriver driver = entry.driver;
                    if (driver == null) {
                        Log.d(TAG, "No driver.");
                        return;
                    }
    
                    showConsoleActivity(driver);
                    finish();
                }
            });
        }
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MESSAGE_REFRESH);
    }

    private void refreshDeviceList() {
        showProgressBar();

        new AsyncTask<Void, Void, List<DeviceEntry>>() {
            @Override
            protected List<DeviceEntry> doInBackground(Void... params) {
                Log.d(TAG, "Refreshing device list ...");

                SystemClock.sleep(1000);
                final List<DeviceEntry> result = new ArrayList<DeviceEntry>();
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    final List<UsbSerialDriver> drivers =
                            UsbSerialProber.probeSingleDevice(mUsbManager, device);
                    Log.d(TAG, "Found usb device: " + device);
                    if (drivers.isEmpty()) {
                        Log.d(TAG, "  - No UsbSerialDriver available.");
                        result.add(new DeviceEntry(device, null));
                    } else {
                        for (UsbSerialDriver driver : drivers) {
                            Log.d(TAG, "  + " + driver);
                            try {
                                driver.setDTR(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            result.add(new DeviceEntry(device, driver));
                            // start to load script automatically when Android find Arduino.
                            startConnectionIfAvailable(device, driver);
                        }
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<DeviceEntry> result) {
                mEntries.clear();
                mEntries.addAll(result);
                mAdapter.notifyDataSetChanged();
                mProgressBarTitle.setText(
                        String.format("%s device(s) found",Integer.valueOf(mEntries.size())));
                hideProgressBar();
                Log.d(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }

            // start to load script automatically when Android find arduino.
            private void startConnectionIfAvailable(UsbDevice device, UsbSerialDriver driver){
                if (driver.getClass().getSimpleName() != null || detectArduinoMicro(device)){
                    showConsoleActivity(driver);
                    finish();
                }
            }

        }.execute((Void) null);
    }
    
    private boolean detectArduinoMicro(UsbDevice device){
        boolean isDetected = false;
        int vid = device.getVendorId();
        if (ARDUINO_MICRO_VID == vid){
            isDetected = true;
        }
        return isDetected;
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarTitle.setText(R.string.refreshing);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void showConsoleActivity(UsbSerialDriver driver) {
        SerialConsoleActivity.show(this, driver);
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
                  resultFacade.setActivity(DeviceListActivity.this);
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
              startService(new Intent(this, ScriptService.class));
            }
            finish();
          }
    }
}
