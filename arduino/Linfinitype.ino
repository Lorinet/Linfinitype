#include <SoftwareSerial.h>

#define THUMB 5
#define INDEX 7
#define MIDDLE 6
#define RING 8
#define LITTLE 9

SoftwareSerial bt(4, 3);

void setup()
{
  delay(3000);
  bt.begin(9600);
  bt.println("Linfinitype 0.2");
  pinMode(THUMB, INPUT_PULLUP);
  pinMode(INDEX, INPUT_PULLUP);
  pinMode(MIDDLE, INPUT_PULLUP);
  pinMode(RING, INPUT_PULLUP);
  pinMode(LITTLE, INPUT_PULLUP);
  delay(1000);
}

void loop()
{
  bt.println(String(digitalRead(THUMB)) + String(digitalRead(INDEX)) + String(digitalRead(MIDDLE)) + String(digitalRead(RING)) + String(digitalRead(LITTLE)));
  delay(500);
}
