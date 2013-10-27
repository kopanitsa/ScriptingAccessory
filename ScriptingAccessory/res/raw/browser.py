import android

droid = android.Android()
droid.makeToast('Play Movie2')
#droid.startActivityIntent(droid.makeIntent('android.intent.action.VIEW', 'http://www.google.co.jp/'))
intent = droid.makeIntent('android.intent.action.VIEW', 'https://docs.google.com/file/d/0B14PZnE7DkYmck9Yc2tlcTZwTnM/edit?usp=sharing').result
droid.startActivityIntent(intent)
        