package com.wesleyrnash.nfcdrawtest.app;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Wesley on 6/24/2014.
 */
public class Read {
    private NTag myTag;
    public byte[] result;

    public static final String TAG = "DrawTest";

    public Read(NTag tag){
        myTag = tag;
    }

    //gets the message and records from the tag
    public void read() throws NullPointerException, SmartCardException, IOException, FormatException{
        NdefMessage ndefMessage = null;
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

    }

    private void readText(NdefRecord record) throws UnsupportedEncodingException {
        //get the payload and id from the record
        byte[] payload = record.getPayload();

        //check which type of encoding the payload is in
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        //extract the message from the payload (get rid of the language code)
        byte[] textBytes = Arrays.copyOfRange(payload, languageCodeLength + 1, payload.length);
        Log.d(TAG, "print out message bytes in Reader");

        result = textBytes;
    }
}
