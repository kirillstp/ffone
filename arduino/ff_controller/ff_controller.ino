

#include <Arduino.h>
#include <ServoTimer2.h>
#include <AltSoftSerial.h>
#include <ctype.h>

#define R_MOTOR_ENC_PIN 3
#define L_MOTOR_ENC_PIN 2
#define L_MOTOR_DIR_PIN 4
#define L_MOTOR_EN_PIN 5
#define BT_TX_PIN 9
#define BT_RX_PIN 8
#define SERVO_PIN 12
#define R_MOTOR_EN_PIN 6
#define R_MOTOR_DIR_PIN 7
#define VOLT_LED_PIN 13
#define VOLT_PIN 0
#define DEBUG true
#define DEBOUNCE 100 // debounce value in msec
#define MIN_VOLTAGE 6.2
#define STEERING_CENTER 1500
#define STEERING_RANGE 450
#define FOUR_WAY_STEERING true
#define FOUR_WAY_STEERING_LIMIT 0.4
#define STEERING_DEADZONE 0.2

int encoderTicksL = 0;
int encoderTicksR = 0;
float rpm = 0;
float batteryLevel_V = 0;
int steeringAngle = 0;
const byte numRecvData = 32;
char recvData[numRecvData];
boolean newData = false;
const byte numSendData = 32;
char sendData[numSendData];
float turnVal = 0.0;
float accelVal = 0.0;
float boostVal = 0.0;
int moveDirection = 0; // 0 - forward, 1 - backward
unsigned long start;
float fourWaySteering_L = 1.0;
float fourWaySteering_R = 1.0;

AltSoftSerial controllerSerial;
ServoTimer2 steerServo;

void setup() {
  // setup BT
  start = millis();
  if (DEBUG == true){
    Serial.begin(57600);
    Serial.println("Debug serial open...");
  }
  controllerSerial.begin(9600);
  

  // setup motor encoders
  attachInterrupt(digitalPinToInterrupt(R_MOTOR_ENC_PIN), onEncoderTickRight, RISING);
  attachInterrupt(digitalPinToInterrupt(L_MOTOR_ENC_PIN), onEncoderTickLeft, RISING);
  // setup motor pins
  pinMode(L_MOTOR_EN_PIN, OUTPUT);
  pinMode(L_MOTOR_DIR_PIN, OUTPUT);
  pinMode(R_MOTOR_EN_PIN, OUTPUT);
  pinMode(R_MOTOR_DIR_PIN, OUTPUT);
  // setup voltmeter
  pinMode(VOLT_LED_PIN, INPUT);
  digitalWrite(VOLT_LED_PIN,LOW);
  // setup servo
  steerServo.attach(SERVO_PIN);
  steer(map(0, -100, 100, STEERING_CENTER-STEERING_RANGE, STEERING_CENTER+STEERING_RANGE));
  // enable motors
  digitalWrite(L_MOTOR_DIR_PIN, 0);
  digitalWrite(R_MOTOR_DIR_PIN, 0);
}

void loop() {
  // get current time
  // read the serial port
  recvBTSerial();
  fourWaySteering_L = 1.0;
  fourWaySteering_R = 1.0;
  if (newData == true && DEBUG == true) {
        
     if (turnVal < 1.0 && turnVal > -1.0) {
      steer(map(turnVal*100, -100, 100, STEERING_CENTER+STEERING_RANGE,STEERING_CENTER-STEERING_RANGE)); // 2nd arg is 4 wheel steering, changing one side motor speed based on the steering angle.

      if (turnVal <= -STEERING_DEADZONE){
        fourWaySteering_L = max(FOUR_WAY_STEERING_LIMIT, 1.0-abs(FOUR_WAY_STEERING_LIMIT*turnVal));
      }
      else if (turnVal >= STEERING_DEADZONE) {
        fourWaySteering_R =  max(FOUR_WAY_STEERING_LIMIT, 1.0-abs(FOUR_WAY_STEERING_LIMIT*turnVal));
      }
      else{
        turnVal = 0;
      }
    }
    //check the values for their validity
    if (accelVal < 1.0 && accelVal > -1.0) {
      if (accelVal > 0){
        moveDirection = 0;
      }
      else {
        moveDirection = 1;
      }
      accelVal = abs(accelVal);

      if (boostVal > 0.5 && boostVal <= 1.0) {
        accelVal = 254.0;
      }
      else {
        
        accelVal = map(accelVal*100, 0, 100, 0, 200);
      }
      if (FOUR_WAY_STEERING == true) {
        Serial.print("TurnValue ");
        Serial.println(turnVal);
        Serial.print("Left ");
        Serial.println( (int) (fourWaySteering_L*accelVal));
        Serial.print("Right ");
        Serial.println((int) (fourWaySteering_R*accelVal));
        motorLeft((int) (fourWaySteering_L*accelVal), moveDirection);
        motorRight((int) (fourWaySteering_R*accelVal), moveDirection);
      }
      else {
        motorLeft((int) accelVal, moveDirection);
        motorRight((int) accelVal, moveDirection);
      }
    }



    // set movement direction based on the sign of the accelVal (- is forwar
    batteryLevel_V = voltMeter();
    if (batteryLevel_V < MIN_VOLTAGE) {
      digitalWrite(VOLT_LED_PIN,HIGH);
    }
    newData = false;

  }

  float time_diff = millis() - start;

  if (time_diff >= DEBOUNCE) {
    rpm = ((encoderTicksL+encoderTicksR)/8)/(time_diff/60000); 
    start = millis();
    encoderTicksL = 0;
    encoderTicksR = 0;
    controllerSerial.print(rpm);
    controllerSerial.print(',');
    controllerSerial.print(rpm);
    controllerSerial.print(',');
    controllerSerial.print(batteryLevel_V);
    controllerSerial.println('>');
  }
}

void recvBTSerial() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
    char ctrlr;
    
    while (controllerSerial.available() > 0 && newData == false) {
        rc = controllerSerial.read();
        if (rc == startMarker) {
          recvData[0] = 0;
          ndx = 0;
          recvInProgress = true;
        }
        else if (rc == endMarker) {
          recvInProgress = false;
          recvData[0] = 0;
          ndx = 0;
          newData = true;
        }
        else if (recvInProgress == true) {
          if (rc == 'x' || rc == 'y' || rc == 'b') {
            ctrlr = rc;
          }
          else if (rc == ',') {
            if (ctrlr == 'x'){
              turnVal = atof(recvData);
            }
            else if (ctrlr == 'y'){
              accelVal = atof(recvData);
            }
            else if (ctrlr == 'b') {
              boostVal = atof(recvData);
            }
            recvData[0] = 0;
            ndx = 0;
          }
          else if (isdigit(rc) == true || rc == '.' || rc == '-' ) {
            recvData[ndx] = rc;
            ndx++;
          }
        }
    }
}

void onEncoderTickLeft(int pwmVal){
  encoderTicksL ++;
}

void onEncoderTickRight(int pwmVal){
  encoderTicksR ++;
}

void motorLeft(int val, int dir){
  // set PWM of the left motor
  analogWrite(L_MOTOR_EN_PIN,val);
  digitalWrite(L_MOTOR_DIR_PIN, dir);
}

void motorRight(int val, int dir){
  // set PWM of the  right motor
  analogWrite(R_MOTOR_EN_PIN,val);
  digitalWrite(R_MOTOR_DIR_PIN, dir);

}

void steer(int input) {
  steerServo.write(input);
  delay(15);
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



