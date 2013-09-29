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
#define LED_RED 2
#define LED_GREEN 3

#define STATE_INIT 0
#define STATE_WRITE 1
#define STATE_WRITE_DONE 2
#define STATE_READ 3

#define STRING_EOS "#_EOS_EOS_EOS_EOS_EOS_"

static int state = STATE_INIT;

static int counter = 0;
void setup() {
  pinMode(LED_RED, OUTPUT); 
  pinMode(LED_GREEN, OUTPUT); 
  Serial.begin(115200);
}

void loop() {
  handleState();
  handleAndroidSignal();

  delay(300);
}

void handleState(){
  switch (state) {
    case STATE_INIT:
      if (Serial.peek() != -1) {
        state = STATE_WRITE;
      }
      break;

    case STATE_WRITE:
      Serial.println("import android,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('Hello, digital io!')");

      state = STATE_WRITE_DONE;
      break;

    case STATE_WRITE_DONE:
     Serial.print(STRING_EOS);

      state = STATE_READ;

    case STATE_READ:
    default:
    break;
  }
}

void handleAndroidSignal(){
  char command = 0;
  if (Serial.peek() != -1) {
    Serial.print("Read: ");
    do {
      command = (byte) Serial.read();
      Serial.print("->");
      Serial.print(byte(command));
      Serial.print("<-\n");
      if (command == (byte)0x01) {
        digitalWrite(LED_RED, HIGH);
        digitalWrite(LED_GREEN, LOW);
      } else if (command == 0x02) {
        digitalWrite(LED_RED, LOW);
        digitalWrite(LED_GREEN, HIGH);
      } else {
        digitalWrite(LED_RED, HIGH);
        digitalWrite(LED_GREEN, HIGH);
      Serial.print("*******");
      Serial.print(byte(command));
      Serial.print("*******\n");
      }
    } while (Serial.peek() != -1);
    Serial.print("\n");
  }
}
