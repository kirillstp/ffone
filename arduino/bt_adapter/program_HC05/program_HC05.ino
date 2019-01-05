#include <SoftwareSerial.h>

SoftwareSerial BTSerial(10, 11); // RX | TX

void setup()
{
  pinMode(13, OUTPUT);    // Let's use an onboard LED as communication activity indicator 
  pinMode(9, OUTPUT);     // Pin to activate the configuration mode (KEY pin of the module)
  digitalWrite(9, HIGH);  // Activate configuration mode in order to accept Hayes commands
  Serial.begin(38400);
  BTSerial.begin(38400);  // HC-05 default speed in AT command more
  delay(1000);
  Serial.println("Enter Hayes commands:");
}

void loop()
{

  // Read the output of the HC-05 and send to the serial monitor of the Arduino 
  if (BTSerial.available()){
    digitalWrite(13, HIGH);
    Serial.write(BTSerial.read());
    digitalWrite(13, LOW);
  }

  // Read what was typed on the serial monitor of the Arduino and send to the HC-05
  if (Serial.available()){
    digitalWrite(13, HIGH);
    BTSerial.write(Serial.read());
    digitalWrite(13, LOW);
  }
}
