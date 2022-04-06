#include <SoftwareSerial.h>

#define THUMB A0
#define INDEX A1
#define MIDDLE A2
#define RING A3
#define LITTLE A6
#define PAD_THUMBINDEX 10
#define PAD_INDEXMIDDLE 15

#define THUMB_THRESHOLD1 1100
#define THUMB_THRESHOLD2 300

#define INDEX_THRESHOLD1 700
#define INDEX_THRESHOLD2 250

#define MIDDLE_THRESHOLD1 800
#define MIDDLE_THRESHOLD2 380

#define RING_THRESHOLD1 800
#define RING_THRESHOLD2 250

#define LITTLE_THRESHOLD1 800
#define LITTLE_THRESHOLD2 200

#define DETECT(finger) ({ int mp = measure(finger); (mp < finger##_THRESHOLD2) + (mp < finger##_THRESHOLD1); })
#define TOUCHING(finger1, finger2) (!digitalRead(PAD_##finger1##finger2))

#define HARD(v) v == 2
#define SOFT(v) v == 1
#define ANY(v) v > 0
#define WEAK_ANY(v) v <= 1

SoftwareSerial bt(7, 5);

int measure(int pin) {
  long m = 0;
  for(int i = 0; i < 10; i++)
  {
    m += analogRead(pin);
  }
  m /= 10;
  return ((int)m - 520) * 4;
}

String decodeGesture()
{
  int thumb = DETECT(THUMB);
  int index = DETECT(INDEX);
  int middle = DETECT(MIDDLE);
  int ring = DETECT(RING);
  int little = DETECT(LITTLE);
  if(TOUCHING(INDEX, MIDDLE)) return "r";
  else if(!thumb && HARD(index) && HARD(middle) && HARD(ring) && HARD(little)) return "a";
  else if(SOFT(thumb) && !index && !middle && !ring && !little) return "b";
  else if(ANY(thumb) && SOFT(index) && SOFT(middle) && SOFT(ring) && SOFT(little)) return "c";
  else if(SOFT(thumb) && !index && HARD(middle) && HARD(ring) && HARD(little)) return "d";
  else if(SOFT(thumb) && HARD(index) && HARD(middle) && HARD(ring) && HARD(little)) return "e";
  else if(SOFT(thumb) && ANY(index) && !middle && !ring && !little) return "f";
  else if(!thumb && !index && HARD(middle) && HARD(ring) && HARD(little) && TOUCHING(THUMB, INDEX)) return "g";
  else if(ANY(thumb) && !index && !middle && HARD(ring) && HARD(little)) return "h";
  else if(ANY(thumb) && HARD(index) && HARD(middle) && HARD(ring) && !little) return "i";
  else if(SOFT(thumb) && HARD(index) && HARD(middle) && HARD(ring) && SOFT(little)) return "j";
  else if(!thumb && !index && !middle && HARD(ring) && HARD(little)) return "k";
  else if(!thumb && !index && HARD(middle) && HARD(ring) && HARD(little) && !TOUCHING(THUMB, INDEX)) return "l";
  else if(ANY(thumb) && SOFT(index) && !middle && SOFT(ring) && ANY(little)) return "m";
  else if(ANY(thumb) && HARD(index) && !middle && SOFT(ring) && ANY(little)) return "n";
  else if(SOFT(thumb) && SOFT(index) && SOFT(middle) && ANY(ring) && HARD(little)) return "o";
  else if(WEAK_ANY(thumb) && !index && SOFT(middle) && HARD(ring) && HARD(little)) return "p";
  else if(WEAK_ANY(thumb) && SOFT(index) && HARD(middle) && HARD(ring) && HARD(little)) return "q";
  else if(HARD(thumb) && HARD(index) && HARD(middle) && HARD(ring) && HARD(little)) return "s";
  else if(HARD(thumb) && !index && !middle && !ring && ANY(little)) return "w";
  else if(!thumb && HARD(index) && !middle && HARD(ring) && !little) return "x";
  else if(!thumb && HARD(index) && HARD(middle) && HARD(ring) && !little) return "y";
  bt.print(String(thumb) + String(index) + String(middle) + String(ring) + String(little) + "\n");
  return "";
  
}

void setup()
{
  delay(3000);
  bt.begin(9600);
  bt.println("Linfinitype 0.2");
  pinMode(THUMB, INPUT);
  pinMode(INDEX, INPUT);
  pinMode(MIDDLE, INPUT);
  pinMode(RING, INPUT);
  pinMode(LITTLE, INPUT);
  pinMode(PAD_THUMBINDEX, INPUT_PULLUP);
  pinMode(PAD_INDEXMIDDLE, INPUT_PULLUP);

  delay(1000);
}

void loop()
{
  bt.print(decodeGesture());
  delay(100);
}
