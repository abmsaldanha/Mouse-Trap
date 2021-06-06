#include <Servo.h>


const int pinoServo = 6;  //PINO DIGITAL UTILIZADO PELO SERVO  
//const int inputPin = 2; //PINO DIGITAL UTILIZADO PELO Sensor

Servo s; //OBJETO DO TIPO SERVO
int pos; //POSIÇÃO DO SERVO


int val = 0;                      // variable for reading the pin status

bool motionDetected = false;      //se detetou movimento no sensor
bool responded = false;           //se já obteve resposta do raspberry

bool timeOut = false;             //se após ser detetado movimento passaram 10seg sem resposta do utilizador
float oldTime = 0;                //usado na contagem de tempo
float totalTime = 0;              //contador de tempo (retorna a 0 se >=10)


void setup (){
  Serial.begin(9600);
  //pinMode(inputPin, INPUT);     // declare sensor as input
  s.attach(pinoServo);            //ASSOCIAÇÃO DO PINO DIGITAL AO OBJETO DO TIPO SERVO
  s.write(0);                     //INICIA O MOTOR NA POSIÇÃO 0º
}

void ClearBuffer(){
  while(Serial.available())
    int _t = Serial.read();
}

bool toggle = true;

void loop() {
  
  
  if(isOpen()){
    val = digitalRead(inputPin);  // read input value
  }
  
  if (val == HIGH /*toggle*/ && !motionDetected && isOpen()) {            // check if the input is HIGH
    toggle = false;
    
    Serial.println("detected");
    responded = false;
    motionDetected = true;
    oldTime = millis();
  } 
  
  if(Serial.available()){
    int incomingInt = (Serial.read()-'0');
    
    ClearBuffer();
    switch(incomingInt){
      case 0:
        if(isOpen()){
          closeDoor();
          resetFlags();
          gotResponse(true);
        }
      break;
      case 1:
        openDoor();
        resetFlags();
        gotResponse(true);
      break;
      default:
        return;
      break;
    }
    delay(1000);
  }
  
  
  if(motionDetected){                         
    float currentTime = millis();
    float deltaTime = currentTime - oldTime;
    oldTime = currentTime;
    totalTime += deltaTime;
    if (totalTime >= 10000){         
      timeOut = true;
      motionDetected = false;
      totalTime = 0;
      Serial.println("timeout");
    }
  }
  
  if(timeOut && !responded){   //se passaram 10segs e não houve resposta do utilizador fecha a porta
    closeDoor();
    resetFlags();
    
    toggle = true;
    delay(5000);
  }
}

void closeDoor(){
  Serial.println("close");
  for(pos = 0; pos < 180; pos++){                       //PARA "pos" IGUAL A 0, ENQUANTO "pos" MENOR QUE 180, INCREMENTA "pos"
    s.write(pos);                                       //ESCREVE O VALOR DA POSIÇÃO QUE O SERVO DEVE GIRAR
    delay(5);                                          //INTERVALO DE 15 MILISSEGUNDOS
  }
  delay(5000);
}

void openDoor(){
  Serial.println("open");
  if( !isOpen()){
    for(pos = 180; pos >= 0; pos--){                    //PARA "pos" IGUAL A 180, ENQUANTO "pos" MAIOR OU IGUAL QUE 0, DECREMENTA "pos"
      s.write(pos);                                     //ESCREVE O VALOR DA POSIÇÃO QUE O SERVO DEVE GIRAR
      delay(5);                                        //INTERVALO DE 15 MILISSEGUNDOS
    }
  }

  resetFlags();
}

bool isOpen() {
  if(s.read() == 0){
    return true;  
  }
  return false;
}

void resetFlags(){
  
  timeOut = false;
  motionDetected = false;
  totalTime = 0;
}

void gotResponse(bool var){
  responded = var;
}
