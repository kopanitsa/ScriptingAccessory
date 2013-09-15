import android,time
droid = android.Android()
droid.makeToast('Hello, intent!')

intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, None).result
droid.sendBroadcastIntent(intent)

