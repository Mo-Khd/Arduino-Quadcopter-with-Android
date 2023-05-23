

#include <Servo.h>
#include <Wire.h> //Include the Wire.h library so we can communicate with the gyro

//Declaring variables
int cal_int;
unsigned long UL_timer;
double gyro_pitch, gyro_roll, gyro_yaw;
double gyro_roll_cal, gyro_pitch_cal, gyro_yaw_cal;
byte highByte, lowByte;

Servo myservo;
Servo myservo1;
Servo myservo2;
Servo myservo3;

int pos = 0;    // variable to store the servo position
int y=0;
char data;  // incoming data

int throttle,roll,pitch,yaw;
int motor1,motor2,motor3,motor4;

void setup(){
                                                
  Wire.begin();                                      //Start the I2C as master
  Serial.begin(9600);                                //Start the serial connetion @ 9600bps
   Serial.setTimeout(10);               // for parseInt to be faster
  
  


  //The gyro is disabled by default and needs to be started
  Wire.beginTransmission(105);                       //Start communication with the gyro (adress 1101001)
  Wire.write(0x20);                                  //We want to write to register 20
  Wire.write(0x0F);                                  //Set the register bits as 00001111 (Turn on the gyro and enable all axis)
  Wire.endTransmission();                            //End the transmission with the gyro
  Wire.beginTransmission(105);                       //Start communication with the gyro (adress 1101001)
  Wire.write(0x23);                                  //We want to write to register 23
  Wire.write(0x80);                                  //Set the register bits as 10000000 (Block Data Update active)
  Wire.endTransmission();                            //End the transmission with the gyro


  delay(250);                                        //Give the gyro time to start
  myservo.attach(3);
  myservo1.attach(5);
  myservo2.attach(6);
  myservo3.attach(9);

  
  //Let's take multiple samples so we can determine the average gyro offset
  Serial.print("Starting calibration...");           //Print message
  for (cal_int = 0; cal_int < 2000 ; cal_int ++){    //Take 2000 readings for calibration
    gyro_signalen();                                 //Read the gyro output
    gyro_roll_cal += gyro_roll;                      //Ad roll value to gyro_roll_cal
    gyro_pitch_cal += gyro_pitch;                    //Ad pitch value to gyro_pitch_cal
    gyro_yaw_cal += gyro_yaw;                        //Ad yaw value to gyro_yaw_cal
    if(cal_int%100 == 0)Serial.print(".");           //Print a dot every 100 readings
    delay(4);                                        //Wait 4 milliseconds before the next loop
  }
  //Now that we have 2000 measures, we need to devide by 2000 to get the average gyro offset
  Serial.println(" done!");                          //2000 measures are done!
  gyro_roll_cal /= 2000;                             //Divide the roll total by 2000
  gyro_pitch_cal /= 2000;                            //Divide the pitch total by 2000
  gyro_yaw_cal /= 2000;                              //Divide the yaw total by 2000
  

 
  
    throttle = 0;
    roll = 0;
    pitch = 0;
    yaw = 0; 
   
    for (pos = 0; pos <= 56; pos += 1) { // goes from 0 degrees to 56 degrees
    // in steps of 1 degree
    
    myservo3.write(pos); 
     myservo.write(pos); 
    myservo1.write(pos); 
    myservo2.write(pos);
    delay(400); 
    
     }
    
  
  }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Main program loop
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void loop(){
gyro_signalen();
  
    //  if(above_cm>2){
    if (Serial.available() > 0) {
      data=Serial.read();
      if(data=='u'){
      int u=Serial.parseInt();
      throttle = u;
       }   // up-down (from zero to positive numbers, increasing number = up)
     // }     
      if(data=='l'){
      int l=Serial.parseInt();
      roll = l;
      }   // left-right (even number = left) 
      if(data=='f'){
      int f=Serial.parseInt();
      pitch = f;
      }   // forward-backward (even number = forward) 
      if(data=='y'){
      int y=Serial.parseInt(); 
      yaw = y;
      }}   // yawing (even number = left/anti-clockwise) 

     

  
 
  //    while(above_cm<=2)
     //   push --;

//        if(above_cm>2)
 //     while(below_cm<=20)
  //      push ++;
    //    while(below_cm>20)
    //      push --;

 //  calculate_pid();

  
  
  
                                                         //The motors are started.
    if (throttle > 1800) {throttle = 1800;}                                   //We need some room to keep full control at full throttle.
    
    motor1 = throttle - (pitch+(gyro_pitch/57.14286)) + (roll+(gyro_roll/57.14286)) - (yaw+(gyro_yaw/57.14286)); //Calculate the pulse for esc 1 (front-right - CCW)
    motor2 = throttle + (pitch+(gyro_pitch/57.14286)) + (roll+(gyro_roll/57.14286)) + (yaw+(gyro_yaw/57.14286)); //Calculate the pulse for esc 2 (rear-right - CW)
    motor3 = throttle + (pitch+(gyro_pitch/57.14286)) - (roll+(gyro_roll/57.14286)) - (yaw+(gyro_yaw/57.14286)); //Calculate the pulse for esc 3 (rear-left - CCW)
    motor4 = throttle - (pitch+(gyro_pitch/57.14286)) - (roll+(gyro_roll/57.14286)) + (yaw+(gyro_yaw/57.14286)); //Calculate the pulse for esc 4 (front-left - CW)

    if(throttle!=0){
    if (motor1 < 1200) motor1 = 1200;                                         //Keep the motors running.
    if (motor2 < 1200) motor2 = 1200;                                         //Keep the motors running.
    if (motor3 < 1200) motor3 = 1200;                                         //Keep the motors running.
    if (motor4 < 1200) motor4 = 1200;                                         //Keep the motors running.
    
    if(motor1 > 2000)motor1 = 2000;                                           //Limit the esc-1 pulse to 2000us.
    if(motor2 > 2000)motor2 = 2000;                                           //Limit the esc-2 pulse to 2000us.
    if(motor3 > 2000)motor3 = 2000;                                           //Limit the esc-3 pulse to 2000us.
    if(motor4 > 2000)motor4 = 2000;                                           //Limit the esc-4 pulse to 2000us.  
  }
  
   
// These numbers is because motors are hot in different seconds 
    myservo.writeMicroseconds(motor1-20); 
    myservo1.writeMicroseconds(motor2+5); 
    myservo2.writeMicroseconds(motor3-50);
    myservo3.writeMicroseconds((motor4+315)*1.075);

    }


void gyro_signalen(){
  Wire.beginTransmission(105);                       //Start communication with the gyro (adress 1101001)
  Wire.write(168);                                   //Start reading @ register 28h and auto increment with every read
  Wire.endTransmission();                            //End the transmission
  Wire.requestFrom(105, 6);                          //Request 6 bytes from the gyro
  while(Wire.available() < 6);                       //Wait until the 6 bytes are received
  lowByte = Wire.read();                             //First received byte is the low part of the angular data
  highByte = Wire.read();                            //Second received byte is the high part of the angular data
  gyro_roll = ((highByte<<8)|lowByte);               //Multiply highByte by 256 and ad lowByte
  if(cal_int == 2000)gyro_roll -= gyro_roll_cal;     //Only compensate after the calibration
  lowByte = Wire.read();                             //First received byte is the low part of the angular data
  highByte = Wire.read();                            //Second received byte is the high part of the angular data
  gyro_pitch = ((highByte<<8)|lowByte);              //Multiply highByte by 256 and ad lowByte
  gyro_pitch *= -1;                                  //Invert axis
  if(cal_int == 2000)gyro_pitch -= gyro_pitch_cal;   //Only compensate after the calibration
  lowByte = Wire.read();                             //First received byte is the low part of the angular data
  highByte = Wire.read();                            //Second received byte is the high part of the angular data
  gyro_yaw = ((highByte<<8)|lowByte);                //Multiply highByte by 256 and ad lowByte
  gyro_yaw *= -1;                                    //Invert axis
  if(cal_int == 2000)gyro_yaw -= gyro_yaw_cal;       //Only compensate after the calibration
}
