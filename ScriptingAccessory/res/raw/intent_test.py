import android,time
droid = android.Android()
#droid.makeToast('Hello, digital io!')

extra1 = {
    "command"   : "digitalWrite",
    "port"      : "2",
    "value"     : "high",
}

extra2 = {
    "command"   : "digitalWrite",
    "port"      : "2",
    "value"     : "low",
}

for i in range(3) :
    intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, extra1).result
    droid.sendBroadcastIntent(intent)
    sleep(1)
    
    intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, extra2).result
    droid.sendBroadcastIntent(intent)
    sleep(1)
    
