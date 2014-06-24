package com.wesleyrnash.nfcdrawtest.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnTouchListener {

    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    Path path;
    ArrayList<MyPoint> points;
    float downx = 0, downy = 0, upx = 0, upy = 0, movex = 0, movey = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.iv_draw);

        Display currentDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        currentDisplay.getSize(size);
        int dw = size.x;
        int dh = size.y;

        bitmap = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        imageView.setImageBitmap(bitmap);

        imageView.setOnTouchListener(this);

        path = new Path();
        points = new ArrayList<MyPoint>();
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int action = motionEvent.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                downx = motionEvent.getX();
                downy = motionEvent.getY();
                points.add(new MyPoint(downx, downy));
                break;
            case MotionEvent.ACTION_MOVE:
                movex = motionEvent.getX();
                movey = motionEvent.getY();
                points.add(new MyPoint(movex, movey));
                createPath();
                canvas.drawPath(path, paint);
                imageView.invalidate();
                updatePath();
            case MotionEvent.ACTION_UP:
                upx = motionEvent.getX();
                upy = motionEvent.getY();
                points.add(new MyPoint(upx, upy));
                createPath();
                canvas.drawPath(path, paint);
                imageView.invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private void createPath(){
        path.reset();
        boolean first = true;
        for(MyPoint point : points){
            if (first){
                first = false;
                path.moveTo(point.x, point.y);
            } else {
                path.lineTo(point.x, point.y);
            }
        }
    }

    private void updatePath(){
        MyPoint lastPoint = points.get(points.size() - 1);
        MyPoint penultimatePoint = points.get(points.size() - 2);
        float distance = (float) Math.sqrt(Math.pow(penultimatePoint.x - lastPoint.x, 2) - Math.pow(penultimatePoint.y - lastPoint.y, 2));
        if(distance < 100){
            points.remove(points.size() - 1);
        }
    }
}
