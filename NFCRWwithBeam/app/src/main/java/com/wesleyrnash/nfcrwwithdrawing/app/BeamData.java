package com.wesleyrnash.nfcrwwithdrawing.app;

//Import statements for all of the library methods used
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.TextView;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;

//The start of the BeamData2 class
public class BeamData extends Activity implements CreateNdefMessageCallback {

    //Initializing the NFC adapter
    private NfcAdapter mNfcAdapter;

    //Initializing each text view field for inputting data
    private TextView checker;
    private TextView net_name;
    private TextView pan_id;
    private TextView encryption_code;
    private TextView chan_freq;

    //Initializing the string that will be the single record sent in the Ndef message
    private String message;

    //Start of the onCreate method (what is actually run when the activity is created)
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //Sets the layout by specifying which XML file is will use
        setContentView(R.layout.activity_beam_data);

        //Setting up the TextViews by finding the ID for each
        checker = (TextView)findViewById(R.id.tv);
        net_name = (TextView) findViewById(R.id.note);
        pan_id = (TextView) findViewById(R.id.note2);
        encryption_code = (TextView) findViewById(R.id.note3);
        chan_freq = (TextView) findViewById(R.id.note4);

        //Takes the text that is input in the TextView and combines them into one String
        //Must first get the text, and then convert to string
        message = net_name.getText().toString()+";"
                    +pan_id.getText().toString()+";"
                    +encryption_code.getText().toString()+";"
                    +chan_freq.getText().toString();

        //Acquires access to the devices NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //Checks to make sure that the NFC adapter is working properly
        if (mNfcAdapter != null) {
            checker.setText("Tap to beam to another NFC device");
        }
        else {
            checker.setText("This phone is not NFC enabled.");
        }

        //Pushes NdefMessages to devices enabled with NFC when in range
        mNfcAdapter.setNdefPushMessageCallback(this, this);

    }

    //Class method that creates the Ndef message
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //Store the characters in the string in a byte array
        //stored as HEX values
        byte[] textBytes = message.getBytes();

        //Creating the actual record by defining the type of record,
        //the type of text for the record, and the actual byte array
        //containing the message
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {}, textBytes);

        //returns an Ndef message using the record created above
        //Can use multiple records because it uses an array of NdefRecords
        //but only uses one for easy manipulation in Arduino
        return new NdefMessage(new NdefRecord[] { textRecord });
    }
}
