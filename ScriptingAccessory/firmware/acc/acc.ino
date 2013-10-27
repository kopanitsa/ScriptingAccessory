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

#define STATE_INIT 0
#define STATE_WRITE_FOR_CONNECT 1
#define STATE_WRITE_FOR_DISCONNECT 2
#define STATE_WRITE_DONE_FOR_CONNECT 3
#define STATE_WRITE_DONE_FOR_DISCONNECT 4
#define STATE_READ 5

#define STRING_EOS "#_EOS_EOS_EOS_EOS_EOS_"
#define COMMAND_START_TO_LOAD 0xFF

#include <AcceleroMMA7361.h>

static int state = STATE_INIT;

AcceleroMMA7361 accelero;
int x;
int y;
int z;
int pre_x;
int cnt = 0;
int ave = 0;
int history[100];
int num_data = 0;
int index = 0;

static int num_for_ave = 20;
static int threshold = 80;

void setup()
{
  Serial.begin(115200);
  accelero.begin(13, 12, 11, 10, A0, A1, A2);
  accelero.setARefVoltage(5.0);                   //sets the AREF voltage to 3.3V
  accelero.setSensitivity(HIGH);                   //sets the sensitivity to +/-1.5G
  accelero.calibrate();
}

void loop()
{
  switch (state) {
    case STATE_INIT:
      if (Serial.peek() == 0xFF ) {
        state = STATE_WRITE_FOR_CONNECT;
      }
      break;

    case STATE_WRITE_FOR_CONNECT:
      Serial.println("import android,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('accelerometer')");
      Serial.print("droid.makeToast('your steps are ");
      Serial.print(cnt);
      Serial.println("')");
      state = STATE_WRITE_DONE_FOR_CONNECT;
      break;

    case STATE_WRITE_DONE_FOR_CONNECT:
      Serial.print(STRING_EOS);
      state = STATE_WRITE_FOR_DISCONNECT;
      break;

    case STATE_WRITE_FOR_DISCONNECT:
      Serial.println("import android,time");
      Serial.println("droid = android.Android()");
      Serial.println("droid.makeToast('Hello, disconnnnnnnect')");
      Serial.print("droid.makeToast('your steps are ");
      Serial.print(cnt);
      Serial.println("')");
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
//  delay(300);
  
  updateAccelerometer();
  
  delay(100);
}

void updateAccelerometer() {
  pre_x = x;
  x = accelero.getXRaw();
  checkWalking(x);
  y = accelero.getYRaw();
  z = accelero.getZRaw();
  //Serial.print("\nx: ");
  //Serial.print(x);
  //Serial.print("\ty: ");
  //Serial.print(y);
  //Serial.print("\tz: ");
  //Serial.print(z);
  //Serial.print("\tcnt: ");
  //Serial.print(cnt);
  //Serial.print("\tnum_data: ");
  //Serial.print(num_data);
  //Serial.print("\tindex: ");
  //Serial.print(index);
  //Serial.print("\tave: ");
  //Serial.print(ave);
}

void checkWalking(int x) {
  calcAve();
  if (abs(ave - x) > threshold){
    cnt++;
  }
}

int calcAve(){
  int d = 0;
  if (num_data < num_for_ave) {
    d = num_data;
    num_data++;
  } else {
    d = num_for_ave; 
  }
  history[index] = x;

  long sum = 0;
  for (int i = 0; i<= d; i++) {
    sum += history[i];
  }
  ave = sum / num_data;
  index++;
  if (index >= num_for_ave) {
    index = 0;
  }
  return ave;
}
