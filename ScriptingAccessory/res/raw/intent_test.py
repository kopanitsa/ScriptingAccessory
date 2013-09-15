import android,time
droid = android.Android()
#droid.makeToast('Hello, digital io!')

extra = {
    "command"   : "digitalOutput",
    "port"      : "2",
    "value"     : "high",
}
#intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, extra, None).result
intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, extra).result
droid.sendBroadcastIntent(intent)

