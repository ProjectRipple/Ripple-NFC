package com.wesleyrnash.nfcdrawtest.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.ntag.NTag;
import com.nxp.nfclib.ntag.NTag203x;
import com.nxp.nfcliblite.Interface.NxpNfcLibLite;
import com.nxp.nfcliblite.Interface.Nxpnfcliblitecallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class MainActivity extends Activity implements View.OnTouchListener {
    private static final String TAG = "DrawTest";
    final int READ_MODE = 0;
    final int WRITE_MODE = 1;
    int mode = WRITE_MODE;

    Context ctx = this;

    Button clearButton;
    Button toggleButton;

    private NTag tag;
    NxpNfcLibLite libInstance = null;

    byte[] imageBytes;

    ImageView imageView;
    Bitmap bitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private LinkedList<Path> paths = new LinkedList<Path>();
    private LinkedList<ArrayList<MyPoint>> pointArrays = new LinkedList<ArrayList<MyPoint>>();
    private ArrayList<MyPoint> points = new ArrayList<MyPoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.iv_draw);

        bitmap = Bitmap.createBitmap(510, 765, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);

        mPath = new Path();
        paths.add(mPath);
        pointArrays.add(points);

        clearButton = (Button) findViewById(R.id.button_clear);
        toggleButton = (Button) findViewById(R.id.button_mode);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearScreen();
            }
        });

        //set click listener for toggle button
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when clicked, the mode changes to whichever mode it is not currently
                if(mode == WRITE_MODE){
                    mode = READ_MODE;
                    toggleButton.setText(R.string.string_readMode);
                } else {
                    mode = WRITE_MODE;
                    toggleButton.setText(R.string.string_writeMode);
                }
            }
        });

        libInstance = NxpNfcLibLite.getInstance();
        libInstance.registerActivity(this);
    }

    private void clearScreen(){
        Log.d(TAG, "clearing screen");
        paths.clear();
        pointArrays.clear();
        mPath = new Path();
        paths.add(mPath);
        points = new ArrayList<MyPoint>();
        pointArrays.add(points);
        mCanvas.drawColor(Color.WHITE);
        imageView.invalidate();
        Log.d(TAG, "done clearing screen");
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

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 10;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        points.add(new MyPoint(mX, mY));
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            points.add(new MyPoint(mX, mY));
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath = new Path();
        paths.add(mPath);

        for(MyPoint point : points){
            Log.d(TAG, "" + (int) point.x + ", " + (int) point.y);
        }
        Log.d(TAG, "" + points.size());
        points = new ArrayList<MyPoint>();
        pointArrays.add(points);
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                imageView.invalidate();
                break;
        }
        updateCanvas();
        return true;
    }

    private void updateCanvas() {

        for (Path p : paths){
            mCanvas.drawPath(p, mPaint);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        libInstance.filterIntent(intent, new Nxpnfcliblitecallback(){

            @Override
            public void onNTag203xCardDetected(NTag203x nTag203x) {
                Log.d(TAG, "ntag203 detected");
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
        Log.d(TAG, "handling tag");
        if (mode == WRITE_MODE){
            Log.d(TAG, "write mode");
            try {
                createImageBytes();
                Write writer = new Write(tag, imageBytes);
                writer.write();
                Log.d(TAG, "write successful");
                //notify the user of successful writing
                Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
                //clear the screen for testing purposes
                clearScreen();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } catch (SmartCardException e) {
                e.printStackTrace();
            }

        } else if (mode == READ_MODE){
            Log.d(TAG, "read mode");
            Read reader = new Read(tag);
            Log.d(TAG, "reader created");
            try {
                reader.read();
            } catch (SmartCardException e) {
                e.printStackTrace();
                Log.d(TAG, "Smart Card Exception");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "IO Exception");
            } catch (FormatException e) {
                e.printStackTrace();
                Log.d(TAG, "Format Exception");
            }
            Log.d(TAG, "message read");
            updateDrawing(reader.result);
        }
    }

    private void createImageBytes(){
        pointArrays.remove(pointArrays.size()-1);
        ArrayList<Byte> imagePoints = new ArrayList<Byte>();
        int x, y;
        for(ArrayList<MyPoint> pointArray : pointArrays){
            imagePoints.add((byte) -127);
            imagePoints.add((byte) -127);
            for(MyPoint point : pointArray){
                x = (int) point.x/2 - 127;
                y = (int) point.y/3 - 127;
                if (x == -127)
                    x++;
                else if (x == 128)
                    x--;
                if (y == -127)
                    y++;
                else if (y == 128)
                    y--;
                byte bytex = (byte) x;
                byte bytey = (byte) y;
                Log.d(TAG, "" + bytex + ", " + bytey);
                imagePoints.add(bytex);
                imagePoints.add(bytey);
            }
        }
        imageBytes = new byte[imagePoints.size()];
        Log.d(TAG, "creating byte array");
        for(int i = 0; i < imagePoints.size(); i++){
            imageBytes[i] = imagePoints.get(i);
            Log.d(TAG, "" + imageBytes[i]);
        }
        Log.d(TAG, "byte array size: " + imageBytes.length);
    }

    private void updateDrawing(byte[] image){
        Log.d(TAG, "imagePoints created");
        float x, y, px = 0, py = 0;
        mPath = new Path();
        paths.add(mPath);
        points = new ArrayList<MyPoint>();
        pointArrays.add(points);
        boolean first = true;
        Log.d(TAG, "loop through byte array");
        for(int i = 2; i < image.length; i += 2){
            Log.d(TAG, "" + i + " of " + image.length);
            if((int) image[i] == -127) {
                Log.d(TAG, "make new path");
                mPath.lineTo(px, py);
                mPath = new Path();
                paths.add(mPath);
                points = new ArrayList<MyPoint>();
                pointArrays.add(points);
                first = true;
            } else {

                x = (((int) image[i]) + 127) * 2;
                y = (((int) image[i+1]) + 127) * 3;
                Log.d(TAG, "add to path: " + x + ", " + y);
                if(first){
                    first = false;
                    mPath.moveTo(x, y);
                    Log.d(TAG, "first");
                } else {
                    mPath.quadTo(px, py, (x + px)/2, (y + py)/2);
                    Log.d(TAG, "not first");
                }
                px = x;
                py = y;
                points.add(new MyPoint(x, y));
            }
        }
        Log.d(TAG, "update canvas");
        updateCanvas();
        imageView.invalidate();
        mPath = new Path();
        paths.add(mPath);
        points = new ArrayList<MyPoint>();
        pointArrays.add(points);
    }

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
}
