package com.wesleyrnash.nfcrwadminmode;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;

import org.msgpack.MessagePack;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lucas_000 on 6/16/2014.
 */
public class Write {

    private int source;
    private final int SOURCE_ADMIN = 0;
    private final int SOURCE_TRIAGE = 1;
    private NTag myTag;
    public String id;
    private String adminId;
    public static final String key = "TestTestTestTest";
    private Key aesKey;
    private Cipher cipher;
    private MessagePack msgPack;
    private Map<String, String> map;

    public static final String TAG = "NFCRW";

    public Write(NTag tag, Map<String, String> _map){
        myTag = tag;
        try{
            aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){
            e.printStackTrace();
        }
        msgPack = new MessagePack();
        map = _map;

        source = SOURCE_TRIAGE;
    }

    public Write(NTag tag, String _id){
        myTag = tag;
        try{
            aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){
            e.printStackTrace();
        }
        adminId = _id;

        source = SOURCE_ADMIN;
    }

    //writes the message to the tag
    public void write() throws IOException, FormatException, NullPointerException, SmartCardException {
        // Get an instance of Ndef for the tag.
        //try {
            myTag.connect();
            if(!myTag.isT2T())
                myTag.formatT2T();

            NdefRecord[] records = {createRecord(getData())};
            NdefMessage message = new NdefMessage(records);

            // Enable I/O
            myTag.writeNDEF(message);
            Log.d(TAG, "Message written");
            myTag.close();
//        } catch (SmartCardException e) {
//            e.printStackTrace();
//            Log.d(TAG, "Smart Card Exception");
//        }
    }

    //get the ID that is currently on the tag so it is not overwritten
    private byte[] getData(){
        //get the message on the tag
        NdefMessage ndefMessage = null;
        try {
            ndefMessage = myTag.readNDEF();
            NdefRecord[] records = ndefMessage.getRecords();
            //check if any records are of the proper type and format
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    //get the ID from the record
                    if(source == SOURCE_ADMIN)
                        return  ndefRecord.getPayload();
                    if(source == SOURCE_TRIAGE)
                        return ndefRecord.getId();
                }
            }
        } catch (SmartCardException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "default".getBytes();
    }

    //turns a string of text into an NDEF formatted record
    private NdefRecord createRecord(byte[] input) throws UnsupportedEncodingException {
        NdefRecord recordNFC = null;

        if (source == SOURCE_ADMIN){
            recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  adminId.getBytes(), input);
        } else if (source == SOURCE_TRIAGE) {
            Log.d(TAG, "creating record: ");

            //set the language
            String lang = "en";

            byte[] msgpackBytes = null;
            try {
                msgpackBytes = msgPack.write(map);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                msgpackBytes = cipher.doFinal(msgpackBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, new String(msgpackBytes));
            Log.d(TAG, "" + msgpackBytes.length);

            byte[] langBytes = lang.getBytes("US-ASCII");
            int langLength = langBytes.length;
            int textLength = msgpackBytes.length;

            //initialize byte array for the payload
            byte[] payload = new byte[1 + langLength + textLength];

            // set status byte (see NDEF spec for actual bits)
            payload[0] = (byte) langLength;

            // copy langbytes and textbytes into payload
            System.arraycopy(langBytes, 0, payload, 1, langLength);
            System.arraycopy(msgpackBytes, 0, payload, 1 + langLength, textLength);

            Log.d(TAG, new String(payload));

            recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, input, payload);
        }

        return recordNFC;
    }
}
