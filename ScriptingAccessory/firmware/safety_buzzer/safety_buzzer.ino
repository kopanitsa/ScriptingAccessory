

void setup() { 
 //Initialize serial and wait for port to open:
  Serial.begin(9600); 
  pinMode(13, OUTPUT);
} 

void loop() { 
  if (Serial) {
    digitalWrite(13, HIGH);
  } else {
    digitalWrite(13, LOW);
  }
 //proceed normally
} 
