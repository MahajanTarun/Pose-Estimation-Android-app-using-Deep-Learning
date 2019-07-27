package com.example.pose_estimation.mimic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Detection {
    private EventListener eventListener = null;
    private ImageClassifier classifier;


    private ArrayList<PointF> points;
    private int color;

    private ArrayList<PointF> prePoints;
    private ArrayList<PointF> allPoints;

    private Context context;
    private Matrix matrix;

    public static boolean isOpenCVInit = false;

    static {
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback mLoaderCallback;

    /**
     * Callback for openCV
     *
     * @param activity
     */
    void setBaseLoadercallback(Context activity) {
        mLoaderCallback = new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        isOpenCVInit = true;
                        break;
                    case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                        break;
                    case LoaderCallbackInterface.INIT_FAILED:
                        break;
                    case LoaderCallbackInterface.INSTALL_CANCELED:
                        break;
                    case LoaderCallbackInterface.MARKET_ERROR:
                        break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }



    public Detection(Activity activity) throws IOException {
        classifier = ImageClassifierFloatInception.create(activity);

        setBaseLoadercallback(activity);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, activity, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


        matrix = new Matrix();
        matrix.postRotate(-90);
        prePoints = new ArrayList<>();
        allPoints = new ArrayList<>();

        context = activity.getApplicationContext();

    }


    /**
     * This method is to set EventListener which will be called on stand up and on count increment
     *
     * @param eventListener
     */
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Pass latest bitmap from camera, this method will process and will detect person and plank pose
     *
     * @param bitmp
     * @return Three points to be displayed
     */
    public ArrayList<PointF> feedFrame(Bitmap bitmp) {
        Bitmap bitmap = Bitmap.createBitmap(bitmp, 0, 0, bitmp.getWidth(), bitmp.getHeight(),
                matrix, true);

        classifier.classifyFrame(Bitmap.createScaledBitmap(bitmap,
                classifier.imageSizeX,
                classifier.imageSizeY, true));
        if (points != null) {
            points.clear();
        }
//        float[][] rotatedPoints = changePointsRotation(classifier.mPrintPointArray);
        ArrayList<PointF> points = getPointsFromArray(classifier.mPrintPointArray,
                bitmap.getWidth(), bitmap.getHeight());

        checkNewPoints(points, bitmap.getWidth(), bitmap.getHeight());

        bitmap.recycle();
//        rotated.recycle();
        return prePoints;
    }

    /**
     * This method will compare new points with previous points and if the difference is greater than
     * two percent than only new point will be considered. This helps to reduce fluctuations of points.
     *
     * @param newPoints
     * @param width
     * @param height
     */
    private void checkNewPoints(ArrayList<PointF> newPoints, int width, int height) {
        if (prePoints.size() == newPoints.size()) {
            for (int i = 0; i < prePoints.size(); i++) {
                PointF prePoint = prePoints.get(i);
                PointF newPoint = newPoints.get(i);
                if (Math.abs(prePoint.x - newPoint.x) >= width * 0.02 | Math.abs(prePoint.y - newPoint.y) >= height * 0.02) {
                    prePoints.set(i, newPoint);
                }
            }
        } else {
            prePoints.clear();
            prePoints.addAll(newPoints);
        }
    }


    public boolean checkInitialPosition(Bitmap bitmp) {
        Bitmap bitmap = Bitmap.createBitmap(bitmp, 0, 0, bitmp.getWidth(), bitmp.getHeight(),
                matrix, true);

        classifier.classifyFrame(Bitmap.createScaledBitmap(bitmap,
                classifier.imageSizeX,
                classifier.imageSizeY, true));
        ArrayList<PointF> points = getPointsFromArray(classifier.mPrintPointArray,
                bitmap.getWidth(), bitmap.getHeight());
        allPoints.clear();
        allPoints.addAll(points);
        boolean flag = true;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
//        int null_count = 0;
        for (int i = 0; i < points.size(); i++) {
            PointF pointF = points.get(i);
//            Log.d(TAG, "width,");
//            if(pointF.x < 0.20*height || pointF.x > 0.80*height || pointF.y < 0.20*width){
//                flag = false;
//                break;
//            }
//            if (points.get(i)==null){
//                //if no point is detected
//                null_count++;
//            }
            if (pointF.x < 0.05 * height && pointF.y < 0.05 * width) {
                flag = false;
                break;
            }

        }
        return flag;
    }

    public ArrayList<PointF> getPoints() {
        return allPoints;
    }

    public ArrayList<PointF> fetchPoints(Bitmap bitmap) {
        classifier.classifyFrame(Bitmap.createScaledBitmap(bitmap,
                classifier.imageSizeX,
                classifier.imageSizeY, true));
        ArrayList<PointF> points = getPointsFromArray(classifier.mPrintPointArray,
                bitmap.getWidth(), bitmap.getHeight());
        return points;
    }

    /**
     * @return color in which line should be displayed
     */
    public int getColor() {
        return color;
    }

    /**
     * This method will return three points shoulder, waist and ankle either of  right side or
     * left side according user's position.
     *
     * @param points
     * @return
     */
    private ArrayList<PointF> getDisplyPoints(ArrayList<PointF> points) {
        ArrayList<PointF> threePoints = new ArrayList<>();
        float calculatedAngle = (float) Math.toDegrees(Math.atan2(threePoints.get(0).x - threePoints.get(1).x, threePoints.get(0).y - threePoints.get(1).y) -
                Math.atan2(threePoints.get(2).x - threePoints.get(1).x, threePoints.get(2).y - threePoints.get(1).y));
        if (calculatedAngle < 0) {
            calculatedAngle = calculatedAngle + 360;
        }

        if (calculatedAngle > 170 && calculatedAngle < 190) {
            //Green
            color = Color.parseColor("#6BFF33");

//            colourOut = "#FF0000";
        } else if (calculatedAngle > 160 && calculatedAngle < 200) {
            //yellow

//            color = Color.parseColor("#FFFF33");
            color = Color.parseColor("#FF0000");
        } else { //calculatedAngle > 170 i.e espected ~180~
            //Red

            color = Color.parseColor("#FF0000");
        }

        return threePoints;
    }

    public static void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private float[][] changePointsRotation(float[][] mPrintPointArray) {
        float[][] newPoints = new float[2][14];
        for (int i = 0; i < 14; i++) {
            newPoints[0][i] = mPrintPointArray[1][i];
            newPoints[1][i] = 96 - mPrintPointArray[0][i];
        }
        return newPoints;
    }

    /**
     * This method will resize coordinates of points according to the width and height of bitmap
     *
     * @param mPrintPoints
     * @param width
     * @param height
     */
    private ArrayList<PointF> getPointsFromArray(float[][] mPrintPoints, int width, int height) {
        ArrayList<PointF> mDrawPoints = new ArrayList<>();
        float tempX, tempY;
        for (int i = 0; i < 14; i++) {
            tempX = mPrintPoints[0][i] / 0.5f * ((float) height / classifier.imageSizeX);
            tempY = mPrintPoints[1][i] / 0.5f * ((float) width / classifier.imageSizeY);
//            Log.d(TAG, "getPointsFromArray: tempX:"+tempX+" tempY: "+tempY);
            mDrawPoints.add(new PointF(tempX, tempY));
        }
        return mDrawPoints;
    }

    public interface EventListener {
        void eventListernerMethod(ArrayList<PointF> prePoints);
    }
}
