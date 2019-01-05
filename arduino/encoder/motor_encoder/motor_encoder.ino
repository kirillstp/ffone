
#include <AltSoftSerial.h>
#include <ServoTimer2.h>
#define LH_ENCODER_A 2
#define LH_EN 12
#define LH_PH 11
#define SERVO_PIN 6


int toggle = 0;
int pwmVal = 0;
int shaftSpeed = 0;
int encoderTicks = 0;
AltSoftSerial controllerSerial;
ServoTimer2 steerServo;

void setup() {  
  Serial.begin(9600);
  pinMode(LH_EN, OUTPUT);
  pinMode(LH_PH, OUTPUT);
  pinMode(LH_ENCODER_A, INPUT);
  attachInterrupt(digitalPinToInterrupt(LH_ENCODER_A), onEncoderTick, RISING);
  controllerSerial.begin(9600);
  steerServo.attach(SERVO_PIN);
}

void loop() {
  shaftSpeed = ((encoderTicks/(4))*60)/75; // reading only one out of 4 transitions - 4cpr.  
  encoderTicks = 0;
  Serial.println(shaftSpeed);
  analogWrite(LH_EN,pwmVal);
  digitalWrite(LH_PH,toggle);
  pwmVal += 10;
  pwmVal = pwmVal%200;
  delay(1000);
}

void onEncoderTick() {
  encoderTicks++;
}

