
#define LH_ENCODER_A 2
#define LH_EN 5
#define LH_PH 4



int toggle = 0;
int pwmVal = 0;
int shaftSpeed = 0;
int encoderTicks = 0;

void setup() {  
  Serial.begin(9600);
  pinMode(LH_EN, OUTPUT);
  pinMode(LH_PH, OUTPUT);
  pinMode(LH_ENCODER_A, INPUT);
  attachInterrupt(digitalPinToInterrupt(LH_ENCODE`R_A), onEncoderTick, RISING);

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

