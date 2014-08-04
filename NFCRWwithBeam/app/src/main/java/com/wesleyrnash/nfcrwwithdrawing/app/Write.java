package com.wesleyrnash.nfcrwwithdrawing.app;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Wesley on 7/7/2014.
 */
public class Write {

    private int source;
    private final int SOURCE_ADMIN = 0;
    private final int SOURCE_TRIAGE = 1;
    private Tag myTag;
    private byte[] imageBytes;
    private String adminId;
    private static final String key = "TestTestTestTest";
    private Key aesKey;
    private Cipher cipher;
    private MessagePack msgPack;
    private Map<String, String> map;

    public static final String TAG = "NFCRW";

    public Write(Tag tag, Map<String, String> _map, byte[] _image){
        myTag = tag;
        try{
            aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){
            e.printStackTrace();
        }
        msgPack = new MessagePack();
        map = _map;
        imageBytes = _image;

        source = SOURCE_TRIAGE;
    }

    public Write(Tag tag, String _id){
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
    public void write() throws IOException, FormatException, NullPointerException {

        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(myTag);

        NdefRecord[] records = { createRecord(getData(ndef)) };
        NdefMessage message = new NdefMessage(records);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    //get the ID that is currently on the tag so it is not overwritten
    private byte[] getData(Ndef ndef){
        //get the message on the tag
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                if(source == SOURCE_ADMIN)
                    return  ndefRecord.getPayload();
                if(source == SOURCE_TRIAGE)
                    return ndefRecord.getId();
            }
        }

        return "default".getBytes();
    }

    //turns a string of text into an NDEF formatted record
    private NdefRecord createRecord(byte[] input) throws UnsupportedEncodingException {

        NdefRecord ndefRecord = null;

        if (source == SOURCE_ADMIN){
            ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  adminId.getBytes(), input);
        } else if (source == SOURCE_TRIAGE) {
            Log.d(TAG, "creating record: ");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Packer packer = msgPack.createPacker(out);

            byte[] msgpackBytes = null;
            try {
                packer.write(imageBytes);
                packer.write(map);
                msgpackBytes = out.toByteArray();
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                msgpackBytes = cipher.doFinal(msgpackBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //set the language
            String lang = "en";

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

            ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, input, payload);
        }

        return ndefRecord;
    }
}
