package com.wesleyrnash.nfcimagetest.app;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Wesley on 6/24/2014.
 */
public class Write {

    private NTag myTag;
    public String image;

    public static final String TAG = "NFCRW";

    public Write(NTag tag, String _image){
        myTag = tag;
        image = _image;
    }

    //writes the message to the tag
    public void write() throws IOException, FormatException, NullPointerException, SmartCardException {
        myTag.connect();
        if(!myTag.isT2T())
            myTag.formatT2T();

        NdefRecord[] records = {createRecord()};
        NdefMessage message = new NdefMessage(records);

        // Enable I/O
        myTag.writeNDEF(message);
        Log.d(TAG, "Message written");
        myTag.close();
    }

    //turns a string of text into an NDEF formatted record
    private NdefRecord createRecord() throws UnsupportedEncodingException {
        Log.d(TAG, "creating record: ");

        //set the language
        String lang = "en";
        byte[] imageBytes = image.getBytes();

        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = imageBytes.length;

        //initialize byte array for the payload
        byte[] payload = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(imageBytes, 0, payload, 1 + langLength, textLength);

        Log.d(TAG, new String(payload));

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, payload);

        return recordNFC;
    }
}