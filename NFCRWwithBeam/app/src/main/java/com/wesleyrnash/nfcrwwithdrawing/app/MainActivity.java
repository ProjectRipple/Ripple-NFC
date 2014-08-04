package com.wesleyrnash.nfcrwwithdrawing.app;

//Import statements for each of the functions called
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

//The start of the MainActivity class
public class MainActivity extends Activity {


    //Initialize the various buttons on the main activity layout
    Button triageButton;
    Button adminButton;
    Button beamDataButton;
    Button beamData2Button;
    Button tagDispatchButton;

    //Initialize context
    Context ctx;

    //Set TAG for debugging purposes
    public static final String TAG = "NFCRW";

    //First method run by the application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        //Finds each button by identifying the ID of each button as defined
        //in the activity_main.xml file
        triageButton = (Button) findViewById(R.id.button_triageMode);
        adminButton = (Button) findViewById(R.id.button_adminMode);
        tagDispatchButton = (Button) findViewById(R.id.button_tag_dispatch);
        beamDataButton = (Button) findViewById(R.id.button_beam_data);
        beamData2Button = (Button) findViewById(R.id.button_beam_data2);

        //Creates a click listener that when pressed starts the triage activity class
        triageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creates an intent for the TriageActivity class so that when called
                //the activity is launched
                Intent intent = new Intent(ctx, TriageActivity.class);
                startActivity(intent);
            }
        });

        //Each of these setups is identical to the one above
        //They just start up different activities
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, AdminActivity.class);
                startActivity(intent);
            }
        });
        tagDispatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, TagDispatch.class);
                startActivity(intent);
            }
        });
        beamDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, BeamData.class);
                startActivity(intent);
            }
        });
        beamData2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, BeamData.class);
                startActivity(intent);
            }
        });
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