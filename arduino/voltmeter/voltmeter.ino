int analog_value = 0.0;
float input_voltage = 0.0;
float battery_voltage = 0.0;
int analogInput = 0;
float R1 = 10; // resistance of R1 (kOhm)
float R2 =  10; // resistance of R2 (kOhm)
int led_pin = 13;
void setup()
{
   Serial.begin(9600);     //  opens serial port, sets data rate to 9600 bps
   pinMode(analogInput, INPUT);
   pinMode(led_pin, OUTPUT);
}
void loop()
{

//Conversion formula for voltage

   analog_value = 0;
   analog_value = analogRead(A1);
   delay(100);
   input_voltage = (analog_value * 5.0) / (1024.0); 
   battery_voltage = input_voltage*(R1+R2)/R2;

   if (battery_voltage <0.0){
    battery_voltage = 0.0;
   }
   Serial.print("Analog reading:  ");
   Serial.println(analog_value);
   Serial.print("Battery voltage:  ");
   Serial.println(battery_voltage);
   if (battery_voltage < 6.2) {
    digitalWrite(led_pin,HIGH);
   }
   else {
    digitalWrite(led_pin,LOW);
   }
   
}
