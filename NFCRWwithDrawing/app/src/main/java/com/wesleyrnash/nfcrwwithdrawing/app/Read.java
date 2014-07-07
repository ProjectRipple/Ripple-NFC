package com.wesleyrnash.nfcrwwithdrawing.app;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import java.io.ByteArrayInputStream;
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
 * Created by Wesley on 7/7/2014.
 */
public class Read {

    private Tag myTag;
    public String id;
    public static final String key = "TestTestTestTest";
    private Key aesKey;
    private Cipher cipher;
    private MessagePack msgPack;
    private Template<Map<String, String>> mapTemplate;
    public Map<String, String> result;
    public byte[] imageResult;

    public static final String TAG = "NFCRW";

    public Read(Tag tag){
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
    public void read() throws NullPointerException, IOException, FormatException {

        Ndef ndef = Ndef.get(myTag);

        //get the message on the tag
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    readText(ndefRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, "Unsupported Encoding");
                }
            }
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

    private void getIdAndText(byte[] data) {
        //check to make sure the message isn't empty
        if (data != null) {
            Log.d(TAG, "data length: " + data.length);

            //create an ArrayList to store the bytes of the message
            ArrayList<Byte> resultBytes = new ArrayList<Byte>();
            try{
                //decrypt the message into a byte array
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                byte[] decoded = cipher.doFinal(data);
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

            ByteArrayInputStream in = new ByteArrayInputStream(decodedBytes);
            Unpacker unpacker = msgPack.createUnpacker(in);

            result = new HashMap<String, String>();
            try {
                imageResult = unpacker.readByteArray();
                result = unpacker.read(mapTemplate);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, result.toString());
        }
    }
}
