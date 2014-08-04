package com.wesleyrnash.nfcrwwithdrawing.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.msgpack.MessagePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TriageActivity extends Activity implements View.OnTouchListener {

    //initialize values for read and write mode and set initial mode to WRITE_MODE
    final int READ_MODE = 0;
    final int WRITE_MODE = 1;
    int mode = WRITE_MODE;

    Context ctx;

    //Initializing the TextView fields for last, first, and tag ID
    TextView lastName;
    TextView firstName;
    TextView tagId;

    //table 1 setup
    //requires setting up the layout and the TextViews
    TableLayout table1;
    TableRow table1Row1;
    TextView table1Row1Time;
    TextView table1Row1BP;
    TextView table1Row1Pulse;
    TextView table1Row1Respiration;

    //table 2 setup
    //Pretty much the same as the above table but has an AutoComplete
    //TextView for the drug database
    TableLayout table2;
    TableRow table2Row1;
    TextView table2Row1Time;
    AutoCompleteTextView table2Row1DS;
    TextView table2Row1Dose;
    TextView table2Row1Other;

    //ArrayLists to contain all the TextViews and their corresponding headers
    //Filled in later by the TextViews
    ArrayList<TextView> textViews;
    ArrayList<String> headers;

    //Declared for debugging purposes
    public static final String TAG = "NFCRW";

    //button to toggle between reading and writing
    Button toggleMode;

    //set up drawing stuff
    //Button that clears the drawing
    Button clearButton;
    ImageView imageView;
    Bitmap bitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private DrawingHandler drawingHandler;

    //Initialize the message pack for actually sending the messages
    MessagePack msgPack;
    //Initializes the map that will be used to find the values
    Map<String, String> map;

    //Initialized for the autocomplete field
    ArrayAdapter<String> stringAdapter;

    //Setting up the NfcAdapter
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag mytag;


    //The method that runs when the activity is started
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the layout based on the XML file ID
        setContentView(R.layout.activity_triage);

        ctx=this;

        msgPack = new MessagePack();
        map = new HashMap<String, String>();

        //initialize UI elements
        lastName = (TextView)findViewById(R.id.et_lastName);
        firstName = (TextView)findViewById(R.id.et_firstName);
        tagId = (TextView) findViewById(R.id.tv_tagID);

        table1 = (TableLayout) findViewById(R.id.tl_table1);
        table2 = (TableLayout) findViewById(R.id.tl_table2);

        table1Row1 = (TableRow) findViewById(R.id.tr_table1_row1);
        table2Row1 = (TableRow) findViewById(R.id.tr_table2_row1);

        table1Row1Time = (TextView) findViewById(R.id.et_table1_row1_time);
        table1Row1BP = (TextView) findViewById(R.id.et_table1_row1_bp);
        table1Row1Pulse = (TextView) findViewById(R.id.et_table1_row1_pulse);
        table1Row1Respiration = (TextView) findViewById(R.id.et_table1_row1_respiration);

        table2Row1Time = (TextView) findViewById(R.id.et_table2_row1_time);
        table2Row1DS = (AutoCompleteTextView) findViewById(R.id.et_table2_row1_ds);
        table2Row1Dose = (TextView) findViewById(R.id.et_table2_row1_dose);
        table2Row1Other = (TextView) findViewById(R.id.et_table2_row1_other);

        //add TextViews and headers to ArrayLists
        textViews = new ArrayList<TextView>();
        headers = new ArrayList<String>();
        textViews.add(lastName);
        headers.add("ln");
        textViews.add(firstName);
        headers.add("fn");

        textViews.add(table1Row1Time);
        headers.add("t1r1t");
        textViews.add(table1Row1BP);
        headers.add("t1r1b");
        textViews.add(table1Row1Pulse);
        headers.add("t1r1p");
        textViews.add(table1Row1Respiration);
        headers.add("t1r1r");

        textViews.add(table2Row1Time);
        headers.add("t2r1t");
        textViews.add(table2Row1DS);
        headers.add("t2r1s");
        textViews.add(table2Row1Dose);
        headers.add("t2r1d");
        textViews.add(table2Row1Other);
        headers.add("t2r1o");

        imageView = (ImageView) findViewById(R.id.iv_draw);

        bitmap = Bitmap.createBitmap(510, 765, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);

        drawingHandler = new DrawingHandler();

        toggleMode = (Button) findViewById(R.id.button_readwrite);
        clearButton = (Button) findViewById(R.id.button_clear);

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

        //Sets up a click listener that clears the screen of the drawing
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Actually calling screen clearing method
                clearScreen();
                //Resets the textViews as well to be nothing
                for(TextView tv : textViews)
                    tv.setText("");
            }
        });

        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        try {
            dataBaseHelper.createDataBase();
            dataBaseHelper.openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new CreateStringArray().execute(dataBaseHelper);

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    private void clearScreen(){
        Log.d(TAG, "clearing screen");
        drawingHandler.clear();
        mCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        imageView.invalidate();
        Log.d(TAG, "done clearing screen");
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawingHandler.touch_start(x, y);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                drawingHandler.touch_move(x, y);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawingHandler.finishPath();
                mCanvas.drawPath(drawingHandler.getPath(), mPaint);
                drawingHandler.startNewPath();
                imageView.invalidate();
                break;
        }
        updateCanvas();
        return true;
    }

    //function for putting the desired drawing on the canvas
    private void updateCanvas() {
        //Loops through each path and draws for each path
        for (Path p : drawingHandler.getPathList()){
            mCanvas.drawPath(p, mPaint);
        }
    }

    //decrypts the message and sets the TextViews to the values specified by the message
    public void updateTextViews(String id, Map<String, String> result){
        //set the ID field
        tagId.setText("Tag ID: " + id);

        //loop through all TextViews in the UI
        for(int i = 0; i < textViews.size(); i++){
            textViews.get(i).setText(result.get(headers.get(i)));
        }
    }

    //create the message to write to the tag from the EditText views.
    private void createMap(){
        //initialize message with a starting character for easier parsing
        map.put("s", "");
        //loop through all EditText views
        for(int i = 0; i < textViews.size(); i++){
            //check if the text is not empty
            if(!textViews.get(i).getText().toString().equals(""))
                //add the header of the view and the text to the message separated by commas
                map.put(headers.get(i), textViews.get(i).getText().toString());
        }
    }

    private void updateDrawing(byte[] image){
        drawingHandler.updateDrawing(image);
        Log.d(TAG, "update canvas");
        updateCanvas();
        imageView.invalidate();
        drawingHandler.startNewPath();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            handleTag();
        }
    }

    private void handleTag(){
        if (mode == WRITE_MODE){
            try {
                Log.d(TAG, "handling tag");
                createMap();
                Log.d(TAG, "created map");
                drawingHandler.createImageBytes();
                Log.d(TAG, "created image bytes");
                Write writer = new Write(mytag, map, drawingHandler.getImageBytes());
                Log.d(TAG, "created writer object");
                writer.write();
                Log.d(TAG, "write successful");
                //notify the user of successful writing
                Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
                //set all the text fields to Test for testing purposes
                for (TextView tv : textViews)
                    tv.setText("Test");
                clearScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mode == READ_MODE){
            Log.d(TAG, "read mode");
            Read reader = new Read(mytag);
            Log.d(TAG, "reader created");
            try {
                reader.read();
                Log.d(TAG, "read successful");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception");
            }
            updateTextViews(reader.id, reader.result);
            Log.d(TAG, "update text views successful");
            updateDrawing(reader.imageResult);
            Log.d(TAG, "update drawing successful");
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

    //These two methods were generated with teh program, so I don't know if they are necessary
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.triage, menu);
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

    private class CreateStringArray extends AsyncTask<DataBaseHelper, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Started loading string array");
        }

        @Override
        protected String[] doInBackground(DataBaseHelper... params) {
            DataBaseHelper dataBaseHelper = params[0];

            Column column = Column.getInstance();
            if(column.getColumn() == null) {
                column.setColumn(dataBaseHelper.getColumn("SUBSTANCENAME"));
                Log.d(TAG, "column was null");
            } else {
                Log.d(TAG, "column was not null");
            }

            return column.getColumn();
        }

        protected void onPostExecute(String[] result) {
            Log.d(TAG, "Done loading string array");
            stringAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, result);
            table2Row1DS.setAdapter(stringAdapter);
            table2Row1DS.setThreshold(3);
        }

    }
}
