package com.wesleyrnash.nfcrwwithdrawing.app;

        import android.app.Activity;
        import android.nfc.NdefMessage;
        import android.nfc.NdefRecord;
        import android.nfc.NfcAdapter;
        import android.nfc.NfcEvent;
        import android.os.Bundle;
        import android.widget.TextView;
        import android.nfc.NfcAdapter.CreateNdefMessageCallback;

public class BeamData extends Activity implements CreateNdefMessageCallback {

    private NfcAdapter mNfcAdapter;
    private TextView mTextView;
    private TextView mNote;
    private TextView mNote2;
    private TextView mNote3;
    private TextView mNote4;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_beam_data);

        mTextView = (TextView)findViewById(R.id.tv);
        mNote = (TextView) findViewById(R.id.note);
        mNote2 = (TextView) findViewById(R.id.note2);
        mNote3 = (TextView) findViewById(R.id.note3);
        mNote4 = (TextView) findViewById(R.id.note4);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter != null) {mTextView.setText("Tap to beam to another NFC device");}
        else { mTextView.setText("This phone is not NFC enabled.");}

        mNfcAdapter.setNdefPushMessageCallback(this, this);

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        byte[] textBytes = mNote.getText().toString().getBytes();
        byte[] textBytes2 = mNote2.getText().toString().getBytes();
        byte[] textBytes3 = mNote3.getText().toString().getBytes();
        byte[] textBytes4 = mNote4.getText().toString().getBytes();

        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {}, textBytes);
        NdefRecord textRecord2 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {}, textBytes2);
        NdefRecord textRecord3 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {}, textBytes3);
        NdefRecord textRecord4 = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {}, textBytes4);
        return new NdefMessage(new NdefRecord[] { textRecord, textRecord2, textRecord3, textRecord4 });
    }
}
