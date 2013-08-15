# Get weather forecast info from livedoor.com and
# send serial event to external device to notify the information with light.
#
# takahiro.okada@gmail.com

import android
import urllib2, json

#### prepare
droid = android.Android()
api_path = 'http://weather.livedoor.com/forecast/webservice/json/v1'

#### location id
tokyo = 130010


def get_weather_forecast(area_id):
    response = urllib2.urlopen(api_path+'?city='+str(area_id))
    html = response.read()
    data = json.loads(html)
    today_weather = data['forecasts'][0]['telop'].encode('utf-8')
    description = data['description']['text'].encode('utf-8')
    ret = {"today_weather" : today_weather,
          "description" : description
          }
    return ret

def main():
    print("==== start ====")
    response = get_weather_forecast(tokyo)
    print(response["today_weather"])
    droid.makeToast(response["description"])
    print("==== end ====")

if __name__ == '__main__':
    main()