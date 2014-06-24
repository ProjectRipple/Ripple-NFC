package com.wesleyrnash.nfcimagetest.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nxp.nfclib.ntag.NTag;
import com.nxp.nfclib.ntag.NTag203x;
import com.nxp.nfcliblite.Interface.NxpNfcLibLite;
import com.nxp.nfcliblite.Interface.Nxpnfcliblitecallback;

import java.util.ArrayList;


public class MainActivity extends Activity {

    final int READ_MODE = 0;
    final int WRITE_MODE = 1;
    int mode = WRITE_MODE;

    private final static String TAG = "Image";

    Context ctx = this;

    Button toggleMode;

    private NTag tag;
    NxpNfcLibLite libInstance = null;

    Button b11;
    Button b12;
    Button b13;
    Button b14;
    Button b15;
    Button b16;
    Button b17;
    Button b18;

    Button b21;
    Button b22;
    Button b23;
    Button b24;
    Button b25;
    Button b26;
    Button b27;
    Button b28;

    Button b31;
    Button b32;
    Button b33;
    Button b34;
    Button b35;
    Button b36;
    Button b37;
    Button b38;

    Button b41;
    Button b42;
    Button b43;
    Button b44;
    Button b45;
    Button b46;
    Button b47;
    Button b48;

    Button b51;
    Button b52;
    Button b53;
    Button b54;
    Button b55;
    Button b56;
    Button b57;
    Button b58;

    Button b61;
    Button b62;
    Button b63;
    Button b64;
    Button b65;
    Button b66;
    Button b67;
    Button b68;

    Button b71;
    Button b72;
    Button b73;
    Button b74;
    Button b75;
    Button b76;
    Button b77;
    Button b78;

    Button b81;
    Button b82;
    Button b83;
    Button b84;
    Button b85;
    Button b86;
    Button b87;
    Button b88;

    ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b11 = (Button) findViewById(R.id.b11);
        b12 = (Button) findViewById(R.id.b12);
        b13 = (Button) findViewById(R.id.b13);
        b14 = (Button) findViewById(R.id.b14);
        b15 = (Button) findViewById(R.id.b15);
        b16 = (Button) findViewById(R.id.b16);
        b17 = (Button) findViewById(R.id.b17);
        b18 = (Button) findViewById(R.id.b18);

        b21 = (Button) findViewById(R.id.b21);
        b22 = (Button) findViewById(R.id.b22);
        b23 = (Button) findViewById(R.id.b23);
        b24 = (Button) findViewById(R.id.b24);
        b25 = (Button) findViewById(R.id.b25);
        b26 = (Button) findViewById(R.id.b26);
        b27 = (Button) findViewById(R.id.b27);
        b28 = (Button) findViewById(R.id.b28);

        b31 = (Button) findViewById(R.id.b31);
        b32 = (Button) findViewById(R.id.b32);
        b33 = (Button) findViewById(R.id.b33);
        b34 = (Button) findViewById(R.id.b34);
        b35 = (Button) findViewById(R.id.b35);
        b36 = (Button) findViewById(R.id.b36);
        b37 = (Button) findViewById(R.id.b37);
        b38 = (Button) findViewById(R.id.b38);

        b41 = (Button) findViewById(R.id.b41);
        b42 = (Button) findViewById(R.id.b42);
        b43 = (Button) findViewById(R.id.b43);
        b44 = (Button) findViewById(R.id.b44);
        b45 = (Button) findViewById(R.id.b45);
        b46 = (Button) findViewById(R.id.b46);
        b47 = (Button) findViewById(R.id.b47);
        b48 = (Button) findViewById(R.id.b48);

        b51 = (Button) findViewById(R.id.b51);
        b52 = (Button) findViewById(R.id.b52);
        b53 = (Button) findViewById(R.id.b53);
        b54 = (Button) findViewById(R.id.b54);
        b55 = (Button) findViewById(R.id.b55);
        b56 = (Button) findViewById(R.id.b56);
        b57 = (Button) findViewById(R.id.b57);
        b58 = (Button) findViewById(R.id.b58);

        b61 = (Button) findViewById(R.id.b61);
        b62 = (Button) findViewById(R.id.b62);
        b63 = (Button) findViewById(R.id.b63);
        b64 = (Button) findViewById(R.id.b64);
        b65 = (Button) findViewById(R.id.b65);
        b66 = (Button) findViewById(R.id.b66);
        b67 = (Button) findViewById(R.id.b67);
        b68 = (Button) findViewById(R.id.b68);

        b71 = (Button) findViewById(R.id.b71);
        b72 = (Button) findViewById(R.id.b72);
        b73 = (Button) findViewById(R.id.b73);
        b74 = (Button) findViewById(R.id.b74);
        b75 = (Button) findViewById(R.id.b75);
        b76 = (Button) findViewById(R.id.b76);
        b77 = (Button) findViewById(R.id.b77);
        b78 = (Button) findViewById(R.id.b78);

        b81 = (Button) findViewById(R.id.b81);
        b82 = (Button) findViewById(R.id.b82);
        b83 = (Button) findViewById(R.id.b83);
        b84 = (Button) findViewById(R.id.b84);
        b85 = (Button) findViewById(R.id.b85);
        b86 = (Button) findViewById(R.id.b86);
        b87 = (Button) findViewById(R.id.b87);
        b88 = (Button) findViewById(R.id.b88);

        buttons = new ArrayList<Button>();
        buttons.add(b11);
        buttons.add(b12);
        buttons.add(b13);
        buttons.add(b14);
        buttons.add(b15);
        buttons.add(b16);
        buttons.add(b17);
        buttons.add(b18);

        buttons.add(b21);
        buttons.add(b22);
        buttons.add(b23);
        buttons.add(b24);
        buttons.add(b25);
        buttons.add(b26);
        buttons.add(b27);
        buttons.add(b28);

        buttons.add(b31);
        buttons.add(b32);
        buttons.add(b33);
        buttons.add(b34);
        buttons.add(b35);
        buttons.add(b36);
        buttons.add(b37);
        buttons.add(b38);

        buttons.add(b41);
        buttons.add(b42);
        buttons.add(b43);
        buttons.add(b44);
        buttons.add(b45);
        buttons.add(b46);
        buttons.add(b47);
        buttons.add(b48);

        buttons.add(b51);
        buttons.add(b52);
        buttons.add(b53);
        buttons.add(b54);
        buttons.add(b55);
        buttons.add(b56);
        buttons.add(b57);
        buttons.add(b58);

        buttons.add(b61);
        buttons.add(b62);
        buttons.add(b63);
        buttons.add(b64);
        buttons.add(b65);
        buttons.add(b66);
        buttons.add(b67);
        buttons.add(b68);

        buttons.add(b71);
        buttons.add(b72);
        buttons.add(b73);
        buttons.add(b74);
        buttons.add(b75);
        buttons.add(b76);
        buttons.add(b77);
        buttons.add(b78);

        buttons.add(b81);
        buttons.add(b82);
        buttons.add(b83);
        buttons.add(b84);
        buttons.add(b85);
        buttons.add(b86);
        buttons.add(b87);
        buttons.add(b88);

        toggleMode = (Button) findViewById(R.id.button_mode);

        //set click listener for toggle button
        toggleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when clicked, the mode changes to whichever mode it is not currently
                if(mode == WRITE_MODE){
                    mode = READ_MODE;
                    toggleMode.setText(R.string.string_readMode);
                } else {
                    mode = WRITE_MODE;
                    toggleMode.setText(R.string.string_writeMode);
                }
            }
        });

        libInstance = NxpNfcLibLite.getInstance();
        libInstance.registerActivity(this);
    }

    public void toggleColor(View v){
        Log.d(TAG, "in toggleColor");
        Drawable border = getResources().getDrawable(R.drawable.border);
        Drawable border2 = getResources().getDrawable(R.drawable.border2);
        for(Button button : buttons){
            if(v.getId() == button.getId()){
                Log.d(TAG, "button match");
                if(button.getBackground().equals(border2)) {
                    Log.d(TAG, "color was black");
                    button.setBackground(border);
                } else {
                    Log.d(TAG, "color was white");
                    button.setBackground(border2);
                }
            }
        }

//        switch (v.getId()){
//            case R.id.b11:
//                if(b11.getSolidColor() == Color.WHITE)
//                    b11.setBackgroundColor(Color.BLACK);
//                else
//                    b11.setBackgroundColor(Color.WHITE);
//        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        libInstance.filterIntent(intent, new Nxpnfcliblitecallback(){

            @Override
            public void onNTag203xCardDetected(NTag203x nTag203x) {
                tag = nTag203x;
                try {
                    handleTag();
                } catch (Throwable t) {
                    Toast.makeText(ctx, "Unknown error, tap again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleTag(){
//        if (mode == WRITE_MODE){
//            try {
//                createMap();
//                Write writer = new Write(nTag, map);
//                writer.write();
//                //notify the user of successful writing
//                Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
//                //set all the text fields to Test for testing purposes
//                for(int i = 0; i < textViews.size(); i++)
//                    textViews.get(i).setText("Test");
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (FormatException e) {
//                e.printStackTrace();
//            } catch (SmartCardException e) {
//                e.printStackTrace();
//            }
//
//        } else if (mode == READ_MODE){
//            Read reader = new Read(nTag);
//            reader.read();
//            updateTextViews(reader.id, reader.result);
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
