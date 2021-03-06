/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.tapioka.android.interpreter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.ForegroundService;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;
import com.googlecode.android_scripting.interpreter.html.HtmlActivityTask;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.tapioka.android.R;
import com.tapioka.android.usbserial.CommandWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * A service that allows scripts and the RPC server to run in the background.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 * @author Manuel Naranjo (manuel@aircable.net)
 */
public class ScriptService extends ForegroundService {
	private final static int NOTIFICATION_ID = NotificationIdFactory.create();
	private final CountDownLatch mLatch = new CountDownLatch(1);
	private final IBinder mBinder;
	private Intent mStartIntent;
	private int mStartId;

	private InterpreterConfiguration mInterpreterConfiguration;
	private RpcReceiverManager mFacadeManager;
    private AndroidProxy mProxy;
    
    //To receive intent from script
    private SerialIoBroadcastReceiver mIoReceiver;
    private IntentFilter mIntentFilter;
    private UsbBroadcastReceiver mUsbReceiver;

    private CommandWriter mCommandWriter;
    
	public class LocalBinder extends Binder {
		public ScriptService getService() {
			return ScriptService.this;
		}
	}
	
	public ScriptService() {
		super(NOTIFICATION_ID);
		mBinder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInterpreterConfiguration = ((BaseApplication) getApplication())
				.getInterpreterConfiguration();
	}

	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
		mStartIntent = intent;
		mStartId = startId;
		String fileName = Script.getFileName(this);
		executeScript(fileName, false);
		
		// register broadcast intent receiver
        mIoReceiver = new SerialIoBroadcastReceiver();
        mIntentFilter = new IntentFilter("com.tapioka.android.usbserial.IO");
        registerReceiver(mIoReceiver, mIntentFilter);
        
        mUsbReceiver = new UsbBroadcastReceiver();
        IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");
        registerReceiver(mUsbReceiver, filter);
        mUsbReceiver.setService(this);
	}
	
	private void executeScript(String fileName, boolean finishService){
        Interpreter interpreter = mInterpreterConfiguration
                .getInterpreterForScript(fileName);
        if (interpreter == null || !interpreter.isInstalled()) {
            mLatch.countDown();
            if (FeaturedInterpreters.isSupported(fileName)) {
                Intent i = new Intent(this, DialogActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(Constants.EXTRA_SCRIPT_PATH, fileName);
                startActivity(i);
            } else {
                Log.e(this, "Cannot find an interpreter for script "
                                + fileName);
            }
            return;
        }

        // Copies script to internal memory.
        fileName = InterpreterUtils.getInterpreterRoot(this).getAbsolutePath()
                + "/" + fileName;
        File script = new File(fileName);
        // TODO(raaar): Check size here!
        if (!script.exists()) {
            //script = FileUtils.copyFromStream(fileName, getResources()
            //        .openRawResource(Script.ID));
            mProxy.shutdown();
            stopSelf(mStartId);
            return;
        }

        if (Script.getFileExtension(this)
                .equals(HtmlInterpreter.HTML_EXTENSION)) {
            HtmlActivityTask htmlTask = ScriptLauncher.launchHtmlScript(script,
                    this, mStartIntent, mInterpreterConfiguration);
            mFacadeManager = htmlTask.getRpcReceiverManager();
            mLatch.countDown();
        } else {
            mProxy = new AndroidProxy(this, null, true);
            mProxy.startLocal();
            mLatch.countDown();
            Runnable r = null;
            if (finishService) {
                r = new Runnable() {
                    @Override
                    public void run() {
                        mProxy.shutdown();
                        stopSelf(mStartId);
                    }
                };
            }
            ScriptLauncher.launchScript(script, mInterpreterConfiguration,
                    mProxy, r);
        }
	}
	
    public void executeDisconnectedScript() {
        executeScript("disconnect.py", true);
    }

	
    @Override
    public void onDestroy() {
        unregisterReceiver(mIoReceiver);
        super.onDestroy();
    }



    public class SerialIoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                byte command = extras.getByte("command");
                if (mCommandWriter != null) {
                    mCommandWriter.writeRaw(command);
                }
            }
        }
    }

	public RpcReceiverManager getRpcReceiverManager() throws InterruptedException {
		mLatch.await();
		if (mFacadeManager==null) { // Facade manage may not be available on startup.
		mFacadeManager = mProxy.getRpcReceiverManagerFactory()
		.getRpcReceiverManagers().get(0);
		}
		return mFacadeManager;
	}

	@Override
	protected Notification createNotification() {
	    Notification notification =
	        new Notification(R.drawable.tapioka_app_icon, this.getString(R.string.loading), System.currentTimeMillis());
	    // This contentIntent is a noop.
	    PendingIntent contentIntent = PendingIntent.getService(this, 0, new Intent(), 0);
	    notification.setLatestEventInfo(this, this.getString(R.string.app_name), this.getString(R.string.loading), contentIntent);
	    notification.flags = Notification.FLAG_AUTO_CANCEL;
		return notification;
	}

	private boolean needsToBeUpdated(String filename, InputStream content) {
		File script = new File(filename);
		FileInputStream fin;
		Log.d("Checking if " + filename + " exists");

		if (!script.exists()) {
			Log.d("not found");
			return true;
		}

		Log.d("Comparing file with content");
		try {
			fin = new FileInputStream(filename);
			int c;
			while ((c = fin.read()) != -1) {
				if (c != content.read()) {
					Log.d("Something changed replacing");
					return true;
				}
			}
		} catch (Exception e) {
			Log.d("Something failed during comparing");
			Log.e(e);
			return true;
		}
		Log.d("No need to update " + filename);
		return false;
	}

	public void setCommandWriter(CommandWriter commandWriter) {
	    mCommandWriter = commandWriter;
    }

}
