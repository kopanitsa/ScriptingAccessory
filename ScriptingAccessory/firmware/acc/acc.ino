#include <AcceleroMMA7361.h>

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
  Serial.begin(9600);
  accelero.begin(13, 12, 11, 10, A0, A1, A2);
  accelero.setARefVoltage(3.3);                   //sets the AREF voltage to 3.3V
  accelero.setSensitivity(LOW);                   //sets the sensitivity to +/-6G
  accelero.calibrate();
}

void loop()
{
  pre_x = x;
  x = accelero.getXRaw();
  checkWalking(x);
  y = accelero.getYRaw();
  z = accelero.getZRaw();
  Serial.print("\nx: ");
  Serial.print(x);
  Serial.print("\ty: ");
  Serial.print(y);
  Serial.print("\tz: ");
  Serial.print(z);
  Serial.print("\tcnt: ");
  Serial.print(cnt);
  Serial.print("\tnum_data: ");
  Serial.print(num_data);
  Serial.print("\tindex: ");
  Serial.print(index);
  Serial.print("\tave: ");
  Serial.print(ave);
  delay(100);                                     //(make it readable)
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
  Serial.print("\tsum: ");
  Serial.print(sum);
  ave = sum / num_data;
  index++;
  if (index >= num_for_ave) {
    index = 0;
  }
  return ave;
}
