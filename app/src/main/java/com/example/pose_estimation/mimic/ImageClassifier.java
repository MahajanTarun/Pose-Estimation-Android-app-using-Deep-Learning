package com.example.pose_estimation.mimic;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.example.pose_estimation.mimic.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.MappedByteBuffer;

import java.nio.channels.FileChannel;

import java.util.PriorityQueue;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.experimental.GpuDelegate;

public abstract class ImageClassifier {
    private int[] intValues;
    protected Interpreter tflite;
    protected ByteBuffer imgData;
    protected GpuDelegate delegate;
    public float[][] filterLabelProbArray;
    public float[][] mPrintPointArray;
    private final PriorityQueue sortedLabels;
    public int imageSizeX;
    public int imageSizeY;
    private final String modelPath;
    private static final String TAG = "TfLiteCameraDemo";


    public ImageClassifier(Activity activity,int imageSizeX,int imageSizeY,String modelPath,int numBytesPerChannel) {
//        intValues = new int[imageSizeX*imageSizeY];
//        tflite = new Interpreter(loadModelFile(activity));
//        imgData = ByteBuffer.allocateDirect();
        super();
        this.imageSizeX = imageSizeX;
        this.imageSizeY = imageSizeY;
        this.modelPath = modelPath;
        this.intValues = new int[this.imageSizeX * this.imageSizeY];
        this.sortedLabels = new PriorityQueue(3, null);
        MappedByteBuffer byteBuffer = null;
        try {
//            this.delegate = new GpuDelegate();
//            Interpreter.Options options = (new Interpreter.Options()).addDelegate(delegate);
            byteBuffer = loadModelFile(activity);
            this.tflite = new Interpreter(byteBuffer);
//            this.tflite = new Interpreter(byteBuffer, options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.imgData = ByteBuffer.allocateDirect(1 * this.imageSizeX * this.imageSizeY * 3 * numBytesPerChannel);
        this.imgData.order(ByteOrder.nativeOrder());
        Log.d("TfLiteCameraDemo", "Created a Tensorflow Lite Image Classifier.");


    }



    public final String classifyFrame(Bitmap bitmap) {
        if(this.tflite == null) {
            Log.e("TfLiteCameraDemo", "Image classifier has not been initialized; Skipped.");
            return "Uninitialized Classifier.";
        }
        else {
            convertBitmapToByteBuffer(bitmap);
            long startTime = SystemClock.uptimeMillis();
            runInference();
            long endTime = SystemClock.uptimeMillis();
            Log.d("TfLiteCameraDemo", "Timecost to run model_pose inference: " + Long.toString(endTime - startTime));
            return Long.toString(endTime - startTime) + "ms";
        }
    }

    public final void close() {
//        delegate.close();
        tflite.close();
        this.tflite = null;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        Resources resources = activity.getResources();
        AssetFileDescriptor fileDescriptor = resources.openRawResourceFd(R.raw.model_pose);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if(imgData != null) {
            imgData.rewind();
            bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            int pixel = 0;
            long startTime = SystemClock.uptimeMillis();
            int var5 = 0;

            for(int i = 0;i<imageSizeX;i++) {
                for(int j = 0; j<imageSizeY; j++) {
                    int v = this.intValues[pixel++];
                    addPixelValue(v);
                }
            }

            long endTime = SystemClock.uptimeMillis();
            Log.d("TfLiteCameraDemo", "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
        }
    }

    protected abstract void addPixelValue(int var1);

    protected abstract float getProbability(int var1);

    protected abstract void setProbability(int var1,Number var2);

    protected abstract float getNormalizedProbability(int var1);

    protected abstract void runInference();



}
