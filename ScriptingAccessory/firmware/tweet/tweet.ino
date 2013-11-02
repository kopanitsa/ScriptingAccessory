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

static int state = STATE_INIT;

void setup() {
  Serial.begin(115200);
}

void loop() {
  switch (state) {
    case STATE_INIT:
      if (Serial.peek() == 0xFF ) {
        state = STATE_WRITE_FOR_CONNECT;
      }
      break;

    case STATE_WRITE_FOR_CONNECT:
      Serial.println("import android,tweepy,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('tweet')");
      Serial.println("consumer_key = '0QMpjJqKINqJ0n8s9Q2Aw'");
      Serial.println("consumer_secret = 'WMUDV079mWQZ7HsQXyfexRl4OLBx22KCfffmoNhk'");
      Serial.println("access_key = '2165047417-HwaxNc10okIhmR32EHN8kI8NsPdWf4JZTb1Hz8W'");
      Serial.println("access_secret = 'tPHDQKT7GQ2Erm2MGpnvxqsIfbrcS5tlNpSM2LhNQ2xJM'");
      Serial.println("auth = tweepy.OAuthHandler(consumer_key, consumer_secret)");
      Serial.println("auth.set_access_token(access_key, access_secret)");
      Serial.println("tweet = tweepy.API(auth_handler=auth)");
      Serial.println("tweet.update_status('Connected to smapon #smapon\\n' + time.ctime())");
      state = STATE_WRITE_DONE_FOR_CONNECT;
      break;

    case STATE_WRITE_DONE_FOR_CONNECT:
      Serial.print(STRING_EOS);
      state = STATE_WRITE_FOR_DISCONNECT;
      break;

    case STATE_WRITE_FOR_DISCONNECT:
      Serial.println("import android,tweepy,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('tweet')");
      Serial.println("consumer_key = '0QMpjJqKINqJ0n8s9Q2Aw'");
      Serial.println("consumer_secret = 'WMUDV079mWQZ7HsQXyfexRl4OLBx22KCfffmoNhk'");
      Serial.println("access_key = '2165047417-HwaxNc10okIhmR32EHN8kI8NsPdWf4JZTb1Hz8W'");
      Serial.println("access_secret = 'tPHDQKT7GQ2Erm2MGpnvxqsIfbrcS5tlNpSM2LhNQ2xJM'");
      Serial.println("auth = tweepy.OAuthHandler(consumer_key, consumer_secret)");
      Serial.println("auth.set_access_token(access_key, access_secret)");
      Serial.println("tweet = tweepy.API(auth_handler=auth)");
      Serial.println("tweet.update_status('Disconnected from smapon #smapon\\n' + time.ctime())");
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

  delay(300);

}
