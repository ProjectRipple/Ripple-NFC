package com.wesleyrnash.nfcrwwithdrawing.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class AdminActivity extends Activity {

    Context ctx;

    //create text view objects
    TextView adminInstructions;
    TextView tagId;

    //define a tag for debugging
    public static final String TAG = "NFCRW";
    //Set the string for the password
    public static final String PASSWORD = "Ripple";

    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag mytag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ctx = this;

        //initialize text views
        adminInstructions = (TextView) findViewById(R.id.tv_adminInstructions);
        tagId = (TextView) findViewById(R.id.et_adminID);

        createPasswordDialog();

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    private void createPasswordDialog(){
        final EditText password = new EditText(ctx);
        password.setHint("Password");
        password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setView(password)
                .setCancelable(false)
                .setTitle("Enter Admin Password");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AdminActivity.this.finish();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredPassword = password.getText().toString();
                if (enteredPassword.equals(PASSWORD)) {
                    dialog.cancel();
                } else {
                    dialog.setTitle("Invalid Password, try again");
                }
            }
        });
    }

    //checks for when a new intent arrives
    @Override
    protected void onNewIntent(Intent intent) {

        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
        try {
            if(mytag==null){
                Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_SHORT).show();
            }else{
                String message = tagId.getText().toString();
                Write writer = new Write(mytag, message);
                writer.write();
                adminInstructions.setText(R.string.ok_writing);
            }
        } catch (IOException e) {
            Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
            //btnWrite.setText(R.string.string_writeMessage);
        } catch (FormatException e) {
            Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
            //btnWrite.setText(R.string.string_writeMessage);
        } catch (NullPointerException e){
            Toast.makeText(ctx, "NULL POINTER EXCEPTION", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            //btnWrite.setText("NULL POINTER EXCEPTION");
        }
    }

    //I'm not to sure why the foreground dispatch needs to be started and stopped in onPause and onResume
    //but I do know that they are necessary and allow the program to have priority to handle intents
    //without the activity chooser popping up
    @Override
    public void onPause(){
        adapter.disableForegroundDispatch(this);

        super.onPause();
    }

    @Override
    public void onResume(){
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        super.onResume();
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
