/* Copyright 2012 Google Inc.
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

// Sample Arduino sketch for use with usb-serial-for-android.
// Prints an ever-increasing counter, and writes back anything
// it receives.
#define STATE_INIT 0
#define STATE_WRITE_FOR_CONNECT 1
#define STATE_WRITE_FOR_DISCONNECT 2
#define STATE_WRITE_DONE_FOR_CONNECT 3
#define STATE_WRITE_DONE_FOR_DISCONNECT 4
#define STATE_READ 5

#define STRING_EOS "#_EOS_EOS_EOS_EOS_EOS_"
#define COMMAND_START_TO_LOAD 0xFF

#define LED_RED 2
#define LED_GREEN 3

static int state = STATE_INIT;

void setup() {
  Serial.begin(115200);
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
}

void loop() {
  switch (state) {
    case STATE_INIT:
      if (Serial.peek() == 0xFF ) {
        state = STATE_WRITE_FOR_CONNECT;
      }
      break;

    case STATE_WRITE_FOR_CONNECT:
        Serial.println("import android");
        Serial.println("import urllib2, json");
        Serial.println("");
        Serial.println("droid = android.Android()");
        Serial.println("api_path = 'http://weather.livedoor.com/forecast/webservice/json/v1'");
        Serial.println("");
        Serial.println("tokyo = 130010");
        Serial.println("");
        Serial.println("");
        Serial.println("def get_weather_forecast(area_id):");
        Serial.println("    response = urllib2.urlopen(api_path+'?city='+str(area_id))");
        Serial.println("    html = response.read()");
        Serial.println("    data = json.loads(html)");
        Serial.println("    today_weather = data['forecasts'][0]['telop'].encode('utf-8')");
        Serial.println("    description = data['description']['text'].encode('utf-8')");
        Serial.println("    ret = {'today_weather' : today_weather,");
        Serial.println("          'description' : description");
        Serial.println("          }");
        Serial.println("    return ret");
        Serial.println("");
        delay(300);
        Serial.println("");
        Serial.println("def send_command(weather):");
        Serial.println("    extra = None");
        Serial.println("    sunny = weather.find('x')");
        Serial.println("    if sunny == 0:");
        Serial.println("        print('NOT SUNNY')");
        Serial.println("        extra = {");
        Serial.println("            'command'   : '0',");
        Serial.println("        }");
        Serial.println("    else:");
        Serial.println("        extra = {");
        Serial.println("            'command'   : '1',");
        Serial.println("        }");
        Serial.println("        print('SUNNY')");
        Serial.println("    intent = droid.makeIntent('com.tapioka.android.usbserial.IO', None, None, extra).result");
        Serial.println("    droid.sendBroadcastIntent(intent)");
        Serial.println("");
        delay(300);
        Serial.println("");
        Serial.println("def main():");
        Serial.println("    print('==== start ====')");
        Serial.println("    response = get_weather_forecast(tokyo)");
        Serial.println("    print(response['today_weather'])");
        Serial.println("    droid.dialogCreateAlert('today weather', response['description'])");
        Serial.println("    droid.dialogShow()");
        Serial.println("    send_command(response['today_weather'])");
        Serial.println("    print('==== end ====')");
        Serial.println("");
        Serial.println("if __name__ == '__main__':");
        Serial.println("    main()");
        delay(300);

      state = STATE_WRITE_DONE_FOR_CONNECT;
      break;

    case STATE_WRITE_DONE_FOR_CONNECT:
      Serial.print(STRING_EOS);
      state = STATE_WRITE_FOR_DISCONNECT;
      break;

    case STATE_WRITE_FOR_DISCONNECT:
      Serial.println("import android,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('disconnect')");
      state = STATE_WRITE_DONE_FOR_DISCONNECT;
      break;

    case STATE_WRITE_DONE_FOR_DISCONNECT:
      Serial.print(STRING_EOS);
      state = STATE_READ;
      break;

    case STATE_READ:
    default:
    break;
  }
  
  fetchCommand();

  delay(300);

}

void fetchCommand() {
  char command = 0;
  if (Serial.peek() != -1) {
    do {
      command = (byte) Serial.read();
      if (command == (byte)0x01) {
        digitalWrite(LED_RED, HIGH);
        digitalWrite(LED_GREEN, LOW);
      } else if (command == (byte)0x00) {
        digitalWrite(LED_RED, LOW);
        digitalWrite(LED_GREEN, HIGH);
      } else {
        digitalWrite(LED_RED, HIGH);
        digitalWrite(LED_GREEN, HIGH);
      }
    } while (Serial.peek() != -1);
  }
}
