import android,time
droid = android.Android()
#droid.makeToast('Hello, digital io!')

extra = {
    "command"   : "digitalWrite",
    "port"      : "2",
    "value"     : "high",
}

intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, extra).result
droid.sendBroadcastIntent(intent)

