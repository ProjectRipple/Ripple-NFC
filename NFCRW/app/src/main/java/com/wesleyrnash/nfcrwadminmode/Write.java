package com.wesleyrnash.nfcrwadminmode;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
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
public class Write {

    Tag myTag;
    String id;
    public static final String key = "TestTestTestTest";
    Key aesKey;
    Cipher cipher;
    MessagePack msgPack;
    Template<Map<String, String>> mapTemplate;
    Map<String, String> map;
    Map<String, String> result;

    public static final String TAG = "NFCRW";

    public Write(Tag tag, Map<String, String> Map){
        myTag = tag;
        try{
            aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){
            e.printStackTrace();
        }
        msgPack = new MessagePack();
        mapTemplate = tMap(TString, TString);
        map = Map;
    }

    //writes the message to the tag
    public void write() throws IOException, FormatException, NullPointerException {
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(myTag);

        //store the ID on the tag
        byte[] id = getId(ndef);

        //create NDEF records
        NdefRecord[] records = { createRecord(id) };
        //put the records into an NDEF message
        NdefMessage message = new NdefMessage(records);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    //get the ID that is currently on the tag so it is not overwritten
    private byte[] getId(Ndef ndef){
        //get the message on the tag
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        //get the records from the message
        NdefRecord[] records = ndefMessage.getRecords();
        //check if any records are of the proper type and format
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                //get the ID from the record
                return ndefRecord.getId();
            }
        }
        //if no ID found, return default
        return "default".getBytes();
    }

    //turns a string of text into an NDEF formatted record
    private NdefRecord createRecord(byte[] id) throws UnsupportedEncodingException {
        Log.d(TAG, "creating record: ");

        //set the language
        String lang       = "en";

        byte[] msgpackBytes = "".getBytes();
        try {
            msgpackBytes = msgPack.write(map);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            msgpackBytes = cipher.doFinal(msgpackBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, new String(msgpackBytes));
        Log.d(TAG, "" + msgpackBytes.length);

        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = msgpackBytes.length;
//        int    textLength = textBytes.length;

        //initialize byte array for the payload
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes,        0, payload, 1,              langLength);
        System.arraycopy(msgpackBytes, 0, payload, 1 + langLength, textLength);
//        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

//        String id = "RippleNFC";

        Log.d(TAG, new String(payload));

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  id, payload);

        return recordNFC;
    }
}
