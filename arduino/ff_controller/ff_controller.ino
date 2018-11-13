#include <Servo.h>
#include <SoftwareSerial.h>

#define R_MOTOR_ENC_PIN 3
#define L_MOTOR_ENC_PIN 2
#define L_MOTOR_DIR_PIN 4
#define L_MOTOR_EN_PIN 5
#define BT_TX_PIN 6
#define BT_RX_PIN 7
#define SERVO_PIN 10
#define R_MOTOR_EN_PIN 11
#define R_MOTOR_DIR_PIN 12
#define VOLT_LED_PIN 13
#define VOLT_PIN 0
#define DEBUG true


int encoderTicksL = 0;
int encoderTicksR = 0;
float batteryLevel_V = 0;
int steeringAngle = 0;
int r_pwm = 0;
int l_pwm = 0;
const byte numRecvData = 32;
char recvData[numRecvData];
boolean newData = false;
const byte numSendData = 32;
char sendData[numSendData];

SoftwareSerial controllerSerial(BT_TX_PIN,BT_RX_PIN);

void setup() {
  // setup BT
  if (DEBUG == true){
    Serial.begin(57600);
    Serial.println("Debug serial open...");
  }
  controllerSerial.begin(9600);
  

  // setup motor encoders
  // setup motor pins
  // setup voltmeter
  pinMode(VOLT_LED_PIN, INPUT);
  // setup servo
  // 
}

void loop() {
  // get current time
  // read the serial port
  recvBTSerial();
  if (newData == true && DEBUG == true) {
    Serial.print("Received new string from controller: ");
    Serial.println(recvData);
    newData = false;
  }
  
  
  // call the functions
  // compute elapsed time 
  // delay for the remainder of the loop timer
  batteryLevel_V = voltMeter();
//  if (DEBUG == true) {
//      Serial.println(batteryLevel_V);
//  }
  controllerSerial.print("27000,26000,");
  controllerSerial.print(batteryLevel_V);
  controllerSerial.println('>');
  delay(16);
}

void recvBTSerial() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
    if (controllerSerial.available() == true && DEBUG == true){
//      Serial.println("Serial available");
    }
    while (controllerSerial.available() > 0 && newData == false) {
        rc = controllerSerial.read();
//        if (DEBUG == true) {
//          Serial.print("Attempted to read BT: ");
//          Serial.println(rc);
//        }
        if (recvInProgress == true) {
            if (rc != endMarker) {
                recvData[ndx] = rc;
                ndx++;
                if (ndx >= numRecvData) {
                    ndx = numRecvData - 1;
                }
            }
            else {
                recvData[ndx] = '\0'; // terminate the string
                recvInProgress = false;
                ndx = 0;
                newData = true;
            }
        }

        else if (rc == startMarker) {
            recvInProgress = true;
        }
    }
}

void onEncoderTickLeft(int pwmVal){
  encoderTicksL ++;
  if (DEBUG == true){
    Serial.print("Encoder left: ");
    Serial.println(encoderTicksL);
  }
}

void onEncoderTickRight(int pwmVal){
  encoderTicksR ++;
  if (DEBUG == true){
    Serial.print("Encoder right: ");
    Serial.println(encoderTicksR);
  }
}

void motorLeft(int pwmVal, int dir){
  // set PWM of the left motor
  if (DEBUG == true){
    Serial.print("Set motor left to ");
    Serial.println(pwmVal);
  }
}

void motorRight(int pwmVal, int dir){
  // set PWM of the  left motor
  if (DEBUG == true){
    Serial.print("Set motor right to ");
    Serial.println(pwmVal);
  }
}

float voltMeter(){
  float analogValue = 0;
  int i = 0;
  for (i; i < 100; i++){
    analogValue += analogRead(VOLT_PIN);
  }
  analogValue = analogValue/i;
  return ((analogValue*5.0)/(1024.0))*2;
}

//void turn(int val){
//  servo.write(val);
//}


