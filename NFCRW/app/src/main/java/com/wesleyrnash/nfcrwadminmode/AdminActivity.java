package com.wesleyrnash.nfcrwadminmode;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdminActivity extends Activity {

    //set up NFC objects
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag mytag;

    Context ctx;

    //create text view objects
    TextView adminInstructions;
    TextView tagId;

    //define a tag for debugging
    public static final String TAG = "NFCRW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ctx = this;

        //initialize text views
        adminInstructions = (TextView) findViewById(R.id.tv_adminInstructions);
        tagId = (TextView) findViewById(R.id.et_adminID);

        //set up an intent filter for when a tag is detected
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    //writes the id to the tag
    private void write(String id, Tag tag) throws IOException, FormatException, NullPointerException {


        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);

        //get the payload that is stored on the tag
        byte[] text = getText(ndef);

        //create NDEF records and turn them into a message
        NdefRecord[] records = { createRecord(text, id) };
        NdefMessage message = new NdefMessage(records);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    //store the text from the tag
    private byte[] getText(Ndef ndef){
        //get the message on the tag
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        //loop through the records
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            //check if a record is of the proper format
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                //return the whole payload
                return ndefRecord.getPayload();
            }
        }
        //if no properly formatted records are found
        return "default".getBytes();
    }

    //create the NDEF record
    private NdefRecord createRecord(byte[] text, String id) throws UnsupportedEncodingException {
        //creates a record with the new id and old text
        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  id.getBytes(), text);
        return recordNFC;
    }

    //checks for when a new intent arrives
    @Override
    protected void onNewIntent(Intent intent){
        //checks that the intent is for an NFC tag
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            //store the information of the tag into an NFC tag object
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
        try {
            //check that the tag was properly read
            if(mytag==null){
                Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_SHORT).show();
            }else{
                //get the id from the user
                String message = tagId.getText().toString();
                //write the id to the tag
                write(message,mytag);
                //tell the user that the id was written successfully
                adminInstructions.setText(R.string.ok_writing);
            }
        } catch (IOException e) {
            Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
        } catch (FormatException e) {
            Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
        } catch (NullPointerException e){
            Toast.makeText(ctx, "NULL POINTER EXCEPTION", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //I'm not to sure why the foreground dispatch needs to be started and stopped in onPause and onResume
    //but I do know that they are necessary and allow the program to have priority to handle intents
    //without the activity chooser popping up
    @Override
    public void onPause(){
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    //These two methods were generated with the program, so I don't know if they are necessary
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
