package com.wesleyrnash.nfcrwadminmode;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tMap;

/**
 * Created by lucas_000 on 6/16/2014.
 */

public class Read {

    private NTag myTag;
    public String id;
    public static final String key = "TestTestTestTest";
    private Key aesKey;
    private Cipher cipher;
    private MessagePack msgPack;
    private Template<Map<String, String>> mapTemplate;
    public Map<String, String> result;

    public static final String TAG = "NFCRW";

    public Read(NTag tag){
        myTag = tag;
        try{
            aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){
            e.printStackTrace();
        }
        msgPack = new MessagePack();
        mapTemplate = tMap(TString, TString);
    }

    //gets the message and records from the tag
    public void read() throws NullPointerException{
        NdefMessage ndefMessage = null;
        try {
            myTag.connect();
            Log.d(TAG, "connected to tag");
            if(!myTag.isT2T())
                myTag.formatT2T();
            Log.d(TAG, "formatted tag");
            ndefMessage = myTag.readNDEF();
            Log.d(TAG, "read tag");
            NdefRecord[] records = ndefMessage.getRecords();
            //check if the records are of acceptable format. If so, read the message
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.d(TAG, "Unsupported Encoding");
                    }
                }
            }
            myTag.close();
        } catch (SmartCardException e) {
            e.printStackTrace();
            Log.d(TAG, "Smart Card Exception");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IO Exception");
        } catch (FormatException e) {
            e.printStackTrace();
            Log.d(TAG, "Format Exception");
        }
    }

    private void readText(NdefRecord record) throws UnsupportedEncodingException {
        //get the payload and id from the record
        byte[] payload = record.getPayload();
        byte[] idBytes = record.getId();
        id = new String(idBytes);

        //check which type of encoding the payload is in
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        //extract the message from the payload (get rid of the language code)
        byte[] textBytes = Arrays.copyOfRange(payload, languageCodeLength + 1, payload.length);

        getIdAndText(textBytes);
    }

    private void getIdAndText(byte[] strings) {
        //check to make sure the message isn't empty
        if (strings != null) {
            Log.d(TAG, new String(strings));
            //create an ArrayList to store the bytes of the message
            ArrayList<Byte> resultBytes = new ArrayList<Byte>();
            try{
                //decrypt the message into a byte array
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                byte[] decoded = cipher.doFinal(strings);
                //store the byte array into the ArrayList
                for(int i = 0; i < decoded.length; i ++)
                    resultBytes.add(i, decoded[i]);
                Log.d(TAG, "decoded them bytes");
            } catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "Exception", e);
            }
            //convert the ArrayList back into a byte array
            byte[] decodedBytes = new byte[resultBytes.size()];
            for(int i = 0; i < resultBytes.size(); i++)
                decodedBytes[i] = resultBytes.get(i);

            result = new HashMap<String, String>();
            try {
                result = msgPack.read(decodedBytes, mapTemplate);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, result.toString());
    }
}


}
