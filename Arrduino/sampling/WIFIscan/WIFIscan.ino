#include "ESP8266WiFi.h"

String accessPoint1, rssi1;
String accessPoint2, rssi2;
String accessPoint3, rssi3;

void setup() {
  Serial.begin(115200);

  // Set WiFi to station mode and disconnect from an AP if it was previously connected
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);

  Serial.println("Setup done");
}

void loop() {
  Serial.println("scan start");

  // WiFi.scanNetworks will return the number of networks found
  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  if (n == 0) {
    Serial.println("no networks found");
  } else {
    Serial.println(" networks found");
    for (int i = 0; i < n; ++i) {
      // Print SSID and RSSI for each network found
      if (WiFi.SSID(i)[0] == 'A' && WiFi.SSID(i)[1] == 'P') {
        int numAP = WiFi.SSID(i)[2];
        switch(numAP) {
          case '1': 
          accessPoint1  = WiFi.SSID(i); 
          rssi1         = WiFi.RSSI(i); 
          Serial.print(accessPoint1);
          Serial.print(" (");
          Serial.print(rssi1);
          Serial.print(")");
          break;
          case '2': 
          accessPoint2  = WiFi.SSID(i); 
          rssi2         = WiFi.RSSI(i); 
          Serial.print(accessPoint2);
          Serial.print(" (");
          Serial.print(rssi2);
          Serial.print(")");
          break;
          default:
          accessPoint3  = WiFi.SSID(i); 
          rssi3         = WiFi.RSSI(i); 
          Serial.print(accessPoint3);
          Serial.print(" (");
          Serial.print(rssi3);
          Serial.print(")");
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
