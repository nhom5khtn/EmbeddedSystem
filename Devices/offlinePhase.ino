#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

const char * ssid     = "HCMUS Public";
const char * password = "";

String ssid1, rssi1;
String ssid2, rssi2;
String ssid3, rssi3;

// Unique IFTTT URL resource
const char* pos11 = "/trigger/pos_1_1/with/key/pZZm1s7SDGnMJma8q4oUWoPyjqFHiN8EOhi7qwANgK2";
const char* pos12 = "/trigger/pos_1_2/with/key/obQtk9UMGrVVeyhHcwb9XM1D7a810z4hXE2EIsvdWTA";
const char* pos13 = "/trigger/pos_1_3/with/key/obQtk9UMGrVVeyhHcwb9XM1D7a810z4hXE2EIsvdWTA";
const char* pos14 = "/trigger/pos_1_4/with/key/dTjiIgbf5Gw6CXkT8hgYC1pUO11XfPv1PeF9UWm4eSq";
const char* pos21 = "/trigger/pos_2_1/with/key/dTjiIgbf5Gw6CXkT8hgYC1pUO11XfPv1PeF9UWm4eSq";
const char* pos22 = "/trigger/pos_2_2/with/key/dTjiIgbf5Gw6CXkT8hgYC1pUO11XfPv1PeF9UWm4eSq";
const char* pos23 = "/trigger/pos_2_3/with/key/baFliOQ8aGMWgXvccJHqa9NJEMhUH0Lfpibo-VpJkWX";
const char* pos24 = "/trigger/pos_2_4/with/key/baFliOQ8aGMWgXvccJHqa9NJEMhUH0Lfpibo-VpJkWX";
const char* pos31 = "/trigger/pos_3_1/with/key/baFliOQ8aGMWgXvccJHqa9NJEMhUH0Lfpibo-VpJkWX";
const char* pos32 = "/trigger/pos_3_2/with/key/q2iexL14AqmCggxrlQu5W";
const char* pos33 = "/trigger/pos_3_3/with/key/q2iexL14AqmCggxrlQu5W";
const char* pos34 = "/trigger/pos_3_4/with/key/q2iexL14AqmCggxrlQu5W";
const char* pos41 = "/trigger/pos_4_1/with/key/b_D8Rb5Ap3-eb_CFrORJqm";
const char* pos42 = "/trigger/pos_4_2/with/key/b_D8Rb5Ap3-eb_CFrORJqm";
const char* pos43 = "/trigger/pos_4_3/with/key/b_D8Rb5Ap3-eb_CFrORJqm";
const char* pos44 = "/trigger/pos_4_4/with/key/pZZm1s7SDGnMJma8q4oUWoPyjqFHiN8EOhi7qwANgK2";
// Maker Webhooks IFTTT
const char* server = "maker.ifttt.com";



void setup() {
  Serial.begin(115200);
  Serial.println("Setup done");
}
void connectWIFI(){
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

void sendData(String resource, String rssi1, String rssi2, String rssi3) {
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
void Scanning(){
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

void loop() {
  DisconnectWIFI();
  Scanning();
  connectWIFI();
  sendData(pos44, rssi1, rssi2, rssi3);
}
