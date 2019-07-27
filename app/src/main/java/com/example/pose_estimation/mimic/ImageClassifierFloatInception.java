package com.example.pose_estimation.mimic;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageClassifierFloatInception extends ImageClassifier {
    private float[][][][] heatMapArray;
    private Mat mMat;
    private int outputW;
    private int outputH;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0F;

    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((float)(pixelValue & 255));
        imgData.putFloat((float)(pixelValue >> 8 & 255));
        imgData.putFloat((float)(pixelValue >> 16 & 255));
    }

    protected float getProbability(int labelIndex) {
//        appendLog("Label: "+labelIndex);
        return 0.0F;
    }

    protected void setProbability(int labelIndex, Number value) {
    }

    protected float getNormalizedProbability(int labelIndex) {
//        appendLog("Probability: " + labelIndex + " : "+this.getProbability(labelIndex));
        return this.getProbability(labelIndex);
    }

    public static void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void runInference() {
        Log.d("||||imgData: ", String.valueOf(imgData.toString().length()));
        Log.d("||||heatMapArray: ", String.valueOf(heatMapArray.toString().length()));
        tflite.run(imgData,heatMapArray);
        Log.d("||||2heatMapArray: ", String.valueOf(heatMapArray.toString().length()));

        if(mPrintPointArray == null){
            mPrintPointArray = new float[2][];
            mPrintPointArray[0] = new float[14];
            mPrintPointArray[1] = new float[14];
        }

        if(!Detection.isOpenCVInit){
            return;
        }

        if(mMat == null){
            mMat = new Mat(outputW,outputH, CvType.CV_32F);
        }

        float[] tempArray = new float[this.outputW * this.outputH];
        float[] outTempArray = new float[this.outputW * this.outputH];


        for(int i = 0;i<14;i++) {
            int index = 0;
            for(int x=0;x<outputW;x++) {
                for(int y=0;y<outputH;y++) {
                    tempArray[index] = this.heatMapArray[0][y][x][i];
                    index++;
                }
            }

            mMat.put(0, 0, tempArray);

            Imgproc.GaussianBlur(mMat, mMat, new Size(5.0, 5.0), 0.0, 0.0);
            mMat.get(0, 0, outTempArray);
            float maxX = 0.0F;
            float maxY = 0.0F;
            float max = 0.0F;

            for(int x=0; x<outputW;x++) {
                for(int y=0; y<outputH;y++) {
                    float top = get(x, y - 1, outTempArray);
                    float left = get(x - 1, y, outTempArray);
                    float right = get(x + 1, y, outTempArray);
                    float bottom = get(x, y + 1, outTempArray);
                    float center = get(x, y, outTempArray);
                    if(center > top && center > left && center > right && center > bottom && (double)center >= 0.01D && center > max) {
                        max = center;
                        maxX = (float)x;
                        maxY = (float)y;
                    }
                }
            }

            if(max == 0.0F) {
                mPrintPointArray = new float[2][];

                mPrintPointArray[0] = new float[14];
                mPrintPointArray[1] = new float[14];
                return;
            }

            mPrintPointArray[0][i] = maxX;
            mPrintPointArray[1][i] = maxY;
            Log.i("TestOutPut", "pic[" + i + "] (" + maxX + ',' + maxY + ") " + max);
        }

    }

    private float get(int x, int y, float[] arr) {
        if(x < 0 || y < 0 || x >= outputW || y >= outputH)
            return  -1.0f;
        else
            return arr[x * outputW + y];
    }

    public static ImageClassifierFloatInception create(Activity activity) throws IOException {
        return new ImageClassifierFloatInception(activity,192,192,96,96,"mv2-cpm.tflite",4);
    }

    private ImageClassifierFloatInception(Activity activity, int imageSizeX, int imageSizeY, int outputW, int outputH, String modelPath, int numBytesPerChannel) throws IOException {
        super(activity, imageSizeX, imageSizeY, modelPath, numBytesPerChannel);
        this.outputW = outputW;
        this.outputH = outputH;
        heatMapArray = new float[1][][][];


        for(int i = 0;i<heatMapArray.length;i++) {

            heatMapArray[i] = new float[outputW][][];
//            Object[] result$iv = new float[outputW][][];


            for(int j = 0;j< outputW; j++) {
                heatMapArray[i][j] = new float[outputH][];
//                int size$iv = this.outputH;
//                Object[] result$iv = new float[size$iv][];
//                int i$iv = 0;

                for(int k=0;k<outputH;k++) {
                    heatMapArray[i][j][k] = new float[14];
//                    result$iv[i$iv] = var27;
                }

            }

        }

    }

}
