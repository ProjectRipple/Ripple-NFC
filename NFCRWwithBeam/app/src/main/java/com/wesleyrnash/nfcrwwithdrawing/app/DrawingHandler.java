package com.wesleyrnash.nfcrwwithdrawing.app;

import android.graphics.Path;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Wesley on 7/7/2014.
 */
public class DrawingHandler {
    private Path mPath;
    private LinkedList<Path> paths = new LinkedList<Path>();
    private ArrayList<MyPoint> points;
    private LinkedList<ArrayList<MyPoint>> pointArrays = new LinkedList<ArrayList<MyPoint>>();

    private byte[] imageBytes;

    public static final String TAG = "NFCRW";

    public DrawingHandler(){
        startNewPath();
    }

    public void clear(){
        paths.clear();
        pointArrays.clear();
        startNewPath();
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 10;

    public void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        points.add(new MyPoint(mX, mY));
    }
    public void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            points.add(new MyPoint(mX, mY));
        }
    }
    public void finishPath(){
        mPath.lineTo(mX, mY);
    }
    public void startNewPath() {
        mPath = new Path();
        paths.add(mPath);
        points = new ArrayList<MyPoint>();
        pointArrays.add(points);
    }

    public void createImageBytes(){
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
        }
        Log.d(TAG, "byte array size: " + imageBytes.length);
    }

    public void updateDrawing(byte[] image){
        Log.d(TAG, "imagePoints created");
        float x, y, px = 0, py = 0;
        startNewPath();
        boolean first = true;
        Log.d(TAG, "loop through byte array");
        for(int i = 2; i < image.length; i += 2){
            if((int) image[i] == -127) {
                mPath.lineTo(px, py);
                startNewPath();
                first = true;
            } else {

                x = (((int) image[i]) + 127) * 2;
                y = (((int) image[i+1]) + 127) * 3;
                Log.d(TAG, "add to path: " + x + ", " + y);
                if(first){
                    first = false;
                    mPath.moveTo(x, y);
                } else {
                    mPath.quadTo(px, py, (x + px)/2, (y + py)/2);
                }
                px = x;
                py = y;
                points.add(new MyPoint(x, y));
            }
        }
    }

    public Path getPath(){
        return mPath;
    }
    public LinkedList<Path> getPathList(){
        return paths;
    }
    public ArrayList<MyPoint> getPoints(){
        return points;
    }
    public LinkedList<ArrayList<MyPoint>> getPointArrays(){
        return pointArrays;
    }
    public byte[] getImageBytes(){
        return imageBytes;
    }
}
