// Receive a NDEF message from a Peer
// Requires SPI. Tested with Seeed Studio NFC Shield v2

#include "SPI.h"
#include "PN532_SPI.h"
#include "snep.h"
#include "NdefMessage.h"

PN532_SPI pn532spi(SPI, 10);
SNEP nfc(pn532spi);
uint8_t ndefBuf[128];

void setup() {
    Serial.begin(9600);
    Serial.println("NFC Peer to Peer Example - Receive Message");
}

void loop() {
    Serial.println("Waiting for message from Peer");    
    int msgSize = nfc.read(ndefBuf, sizeof(ndefBuf));    
    if (msgSize > 0) {      
        NdefMessage msg  = NdefMessage(ndefBuf, msgSize);        
        NdefRecord record = msg.getRecord(0);        
        int payloadLength = record.getPayloadLength();        
        byte payload[payloadLength];        
        record.getPayload(payload);           
        
        String headers[4] = {"Network Name", 
                              "Pan ID", 
                              "Encryption Code", 
                              "Channel Frequency" }; 
        //msg.print();        
        
        char buffer;
        int headersIndex=1;
        Serial.print(headers[0]+": ");        
        for(int i=0;i<payloadLength;i++)
        {
          buffer =(char) payload[i];
          if(buffer!=';')
          {
          Serial.print(buffer);
          }else{
            Serial.print("\n"+headers[headersIndex]+": ");
            headersIndex++;
          }
        }
        
       Serial.println("\nSuccess");
    } else {
        Serial.println("Failed");
    }
    delay(3000);
}

