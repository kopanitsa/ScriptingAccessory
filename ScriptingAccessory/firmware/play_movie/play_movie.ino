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
      Serial.println("import android,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('Play Movie')");
      Serial.println("droid.startActivityIntent(droid.makeIntent('android.intent.action.VIEW', 'https://docs.google.com/file/d/0B14PZnE7DkYmck9Yc2tlcTZwTnM/edit?usp=sharing').result)");
      state = STATE_WRITE_DONE_FOR_CONNECT;
      break;

    case STATE_WRITE_DONE_FOR_CONNECT:
      Serial.print(STRING_EOS);
      state = STATE_WRITE_FOR_DISCONNECT;
      break;

    case STATE_WRITE_FOR_DISCONNECT:
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
