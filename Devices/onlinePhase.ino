#define TRIGGER 5 //D1 on board
#define ECHO    4 //D2 on board
#define ALERT_LED 14 //D5 on board

#include <ESP8266WiFi.h>

const char * ssid     = "Con Cho";
const char * password = "em0biet123";

// Maker Webhooks IFTTT
const char* server = "maker.ifttt.com";
const char* testObserved = "https://maker.ifttt.com/trigger/observed/with/key/kJSsZ6pRJ86wHmbbECDqLZGSphzZLjJiz3Cb47A_Ka-";
//const char* officialObserved = "https://maker.ifttt.com/trigger/observedPos/with/key/kJSsZ6pRJ86wHmbbECDqLZGSphzZLjJiz3Cb47A_Ka-";

String ssid1, rssi1;
String ssid2, rssi2;
String ssid3, rssi3;
float refDistance = 40;
bool isEmpty; 


void setup() {
  Serial.begin (115200);
  pinMode(TRIGGER, OUTPUT);
  pinMode(ECHO, INPUT);
  pinMode(ALERT_LED, OUTPUT);
  Serial.println("Setup done");
}

void ConnectWIFI(){
  delay(10);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  Serial.println("Started");
  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
}
void DisconnectWIFI(){
  // Set WiFi to station mode and disconnect from an AP if it was previously connected
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);
}

void SendData(String resource, String rssi1, String rssi2, String rssi3) {
  Serial.print("Connecting to "); 
  Serial.print(server);
  
  WiFiClient client;
  int retries = 5;
  while(!!!client.connect(server, 80) && (retries-- > 0)) {
    Serial.print(".");
  }
  Serial.println();
  if(!!!client.connected()) {
    Serial.println("Failed to connect...");
  }
  Serial.print("Request resource: "); 
  Serial.println(resource);
  String jsonObject = String("{\"value1\":\"") + rssi1 + "\",\"value2\":\"" + rssi2 + "\",\"value3\":\"" + rssi3 + "\"}";
  client.println(String("POST ") + resource + " HTTP/1.1");
  client.println(String("Host: ") + server); 
  client.println("Connection: close\r\nContent-Type: application/json");
  client.print("Content-Length: ");
  client.println(jsonObject.length());
  client.println();
  client.println(jsonObject);
        
  int timeout = 5 * 10; // 5 seconds             
  while(!!!client.available() && (timeout-- > 0)){
    delay(100);
  }
  if(!!!client.available()) {
    Serial.println("No response...");
  }
  while(client.available()){
    Serial.write(client.read());
  }
  
  Serial.println("\nclosing connection");
  client.stop(); 
}
void ScanningWIFI(){
  Serial.println("scan start");

  // WiFi.scanNetworks will return the number of networks found
  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  if (n == 0) {
    Serial.println("no networks found");
  } else {
    Serial.println(" networks found");
    for (int i = 0; i < n; i++) {
      // Print SSID and RSSI for each network found
      if (WiFi.SSID(i)[0] == 'A' && WiFi.SSID(i)[1] == 'P') {
        char numAP = WiFi.SSID(i)[2];
        switch(numAP) {
          case '1': 
          ssid1  = WiFi.SSID(i); 
          rssi1  = WiFi.RSSI(i); 
          Serial.print(ssid1);
          Serial.print(" (");
          Serial.print(rssi1);
          Serial.print(")");
          break;
          case '2': 
          ssid2  = WiFi.SSID(i); 
          rssi2  = WiFi.RSSI(i); 
          Serial.print(ssid2);
          Serial.print(" (");
          Serial.print(rssi2);
          Serial.print(")");
          break;
          case '3':
          ssid3  = WiFi.SSID(i); 
          rssi3  = WiFi.RSSI(i); 
          Serial.print(ssid3);
          Serial.print(" (");
          Serial.print(rssi3);
          Serial.print(")");
          break;
          default:
          break;
        }
      }
      delay(10);
    }
  }
  Serial.println("");

  // Wait a bit before scanning again
  delay(5000);
}

float ScanningObject(float refDistance){
  unsigned long duration;
  float distance;
  digitalWrite(TRIGGER, LOW);  
  delayMicroseconds(2); 
  
  digitalWrite(TRIGGER, HIGH);
  delayMicroseconds(10); 
  digitalWrite(TRIGGER, LOW);
  
  duration = pulseIn(ECHO, HIGH);
  distance = (duration/2)/29.412;
  return distance;
}


void loop() {
  float distance = ScanningObject(refDistance);

 if(distance > refDistance){
  digitalWrite(ALERT_LED, HIGH);
  Serial.println("This item is out of stock soon!");
  if(!isEmpty){
    isEmpty = true;
    Serial.println("Send notification!");
    ScanningWIFI();
    ConnectWIFI();
    SendData(testObserved, rssi1, rssi2, rssi3);
    DisconnectWIFI();
    }  
 }
 else{
  digitalWrite(ALERT_LED, LOW);
  Serial.println("This item is not out of stock!");
  isEmpty = false;
 }
 Serial.print(distance);
 Serial.println(" cm");
 Serial.print("isEmpty: ");
 Serial.println(isEmpty);
 Serial.println("");
 
 delay(2000);
}
