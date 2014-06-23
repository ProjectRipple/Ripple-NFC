package com.wesleyrnash.nfcrwadminmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.nfc.FormatException;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;
import com.nxp.nfclib.ntag.NTag203x;
import com.nxp.nfcliblite.Interface.NxpNfcLibLite;
import com.nxp.nfcliblite.Interface.Nxpnfcliblitecallback;

import java.io.IOException;

public class AdminActivity extends Activity {

    Context ctx;

    //create text view objects
    TextView adminInstructions;
    TextView tagId;

    //define a tag for debugging
    public static final String TAG = "NFCRW";
    public static final String PASSWORD = "Ripple";

    NxpNfcLibLite libInstance = null;
    private NTag nTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ctx = this;

        //initialize text views
        adminInstructions = (TextView) findViewById(R.id.tv_adminInstructions);
        tagId = (TextView) findViewById(R.id.et_adminID);

        createPasswordDialog();

        libInstance = NxpNfcLibLite.getInstance();
        libInstance.registerActivity(this);
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
        libInstance.filterIntent(intent, new Nxpnfcliblitecallback(){

            @Override
            public void onNTag203xCardDetected(NTag203x nTag203x) {
                Log.d(TAG, "NTAG203 found");
                nTag = nTag203x;
                try {
                    handleTag();
                } catch (Throwable t) {
                    Toast.makeText(ctx, "Unknown error, tap again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleTag(){
        try {
            String message = tagId.getText().toString();
            Write writer = new Write(nTag, message);
            writer.write();
            //notify the user of successful writing
            adminInstructions.setText(R.string.ok_writing);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (SmartCardException e) {
            e.printStackTrace();
        }
    }

    //I'm not to sure why the foreground dispatch needs to be started and stopped in onPause and onResume
    //but I do know that they are necessary and allow the program to have priority to handle intents
    //without the activity chooser popping up
    @Override
    public void onPause(){
        libInstance.stopForeGroundDispatch();

        super.onPause();
    }

    @Override
    public void onResume(){
        libInstance.startForeGroundDispatch();

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
