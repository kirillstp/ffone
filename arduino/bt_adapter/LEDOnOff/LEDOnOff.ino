#include <SoftwareSerial.h>
 
SoftwareSerial mySerial(6, 7);
int dataFromBT;
 
void setup() {
  Serial.begin(57600);
  Serial.println("LEDOnOff Starting...");
 
  // The data rate for the SoftwareSerial port needs to 
  // match the data rate for your bluetooth board.
  mySerial.begin(9600);
  pinMode(13, OUTPUT);   
}
 
void loop() {
  if (mySerial.available()) {
    dataFromBT = mySerial.read();
    Serial.println(dataFromBT);
  }
  if (dataFromBT == '0') {
    // Turn off LED
    digitalWrite(13, LOW);
  } else if (dataFromBT == '1') {
    // Turn on LEFD
    digitalWrite(13, HIGH);
  }
}
