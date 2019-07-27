package com.example.pose_estimation.mimic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class DrawView extends View {

    public static final int COLOR_TOP = Color.parseColor("#980000");
    public static final int COLOR_NECK = Color.parseColor("#ff0000");
    public static final int COLOR_RSHOULDER = Color.parseColor("#ff9900");
    public static final int COLOR_RELBOW = Color.parseColor("#ffff00");
    public static final int COLOR_RWRIST = Color.parseColor("#00ff00");
    public static final int COLOR_LSHOULDER = Color.parseColor("#00ffff");
    public static final int COLOR_LELBOW = Color.parseColor("#4a86e8");
    public static final int COLOR_LWRIST = Color.parseColor("#0000ff");
    public static final int COLOR_RHIP = Color.parseColor("#9900ff");
    public static final int COLOR_RKNEE = Color.parseColor("#274e13");
    public static final int COLOR_RANKLE = Color.parseColor("#e6b8af");
    public static final int COLOR_LHIP = Color.parseColor("#0c343d");
    public static final int COLOR_LKNEE = Color.parseColor("#1c4587");
    public static final int COLOR_LANKLE = Color.parseColor("#073763");
    public static final int COLOR_BACKGROUND = Color.parseColor("#20124d");
    public int color =  Color.parseColor("#ff0000");

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private final CopyOnWriteArrayList<PointF> mDrawPoint = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<PointF> tempArray = new CopyOnWriteArrayList<>();
    private int mWidth, mHeight;
    private float mRatioX, mRatioY;
    private int mImgWidth, mImgHeight;
    public boolean drawRect = true;

    private final int[] mColorArray = new int[]{
            COLOR_TOP, COLOR_NECK,
            COLOR_RSHOULDER, COLOR_RELBOW, COLOR_RWRIST,
            COLOR_LSHOULDER, COLOR_LELBOW, COLOR_LWRIST,
            COLOR_RHIP, COLOR_RKNEE, COLOR_RANKLE,
            COLOR_LHIP, COLOR_LKNEE, COLOR_LANKLE,
            COLOR_BACKGROUND
    };
    private Paint mPaint;

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImgSize(int width, int height) {
        mImgWidth = width;
        mImgHeight = height;
        requestLayout();
    }


    public void setDrawPoint(float[][] point, float ratio) {
        mDrawPoint.clear();
        tempArray.clear();

        float tempX, tempY;
        for (int i = 0; i < 14; i++) {
            tempX = point[0][i] / ratio / mRatioX;
            tempY = point[1][i] / ratio / mRatioY;
            mDrawPoint.add(new PointF(tempX, tempY));
        }
    }

    public void setPointArray(ArrayList<PointF> points){
        mDrawPoint.clear();
        for(int i=0;i<points.size();i++){
            mDrawPoint.add(points.get(i));
        }

    }


    public void setColor(int color){
        this.color = color;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
     * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Paint paint = new Paint();
        int width,height;
//Draw Overlay

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT>27) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
            Log.d(">>>>> ", width +", "+height);
        }
        else {
            width = canvas.getWidth();
            height = canvas.getHeight();
        }

        Log.d(">>>>>canvas ", canvas.getWidth()+", "+canvas.getHeight());


        Bitmap bitmap2 = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);

        Canvas temp = new Canvas(bitmap2);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(10);
            mPaint.setAntiAlias(true);
        }

        if(drawRect){
            mPaint.setColor(color);
            canvas.drawRect(mWidth*0.10f,mHeight*0.05f,mWidth*0.90f,mHeight*0.95f,mPaint);
        }
        else {

            canvas.drawColor(Color.TRANSPARENT);
            paint.setStrokeWidth(5);

            if (mDrawPoint.size() <= 0)
                return;
            paint.setColor(color);
            temp.drawLine(mDrawPoint.get(0).x, mDrawPoint.get(0).y, mDrawPoint.get(1).x, mDrawPoint.get(1).y, paint);
            temp.drawLine(mDrawPoint.get(2).x, mDrawPoint.get(2).y, mDrawPoint.get(1).x, mDrawPoint.get(1).y, paint);
            for (PointF pointF : mDrawPoint) {
                paint.setColor(Color.parseColor("#ffffff"));
                temp.drawCircle(pointF.x, pointF.y, 35, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
                temp.drawCircle(pointF.x, pointF.y, 30, paint);
            }
        }

        Paint paintNew = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap2, 0 , 0, paintNew);
    }



    public Point getPoint(PointF a, PointF b){
        Point point = new Point();
        double m =35;
        double length = Math.sqrt(((a.x-b.x)*(a.x-b.x))+((a.y-b.y)*(a.y-b.y)));
        double n = length - 35;

        point.x = (int) (((m*a.x)+(n*b.x))/(m+n));
        point.y = (int) (((m*a.y)+(n*b.y))/(m+n));

        return point;
    };


    public void drawLine(Canvas temp, Paint paint, PointF a, PointF b){
        paint.setColor(Color.parseColor("#ffffff"));
        temp.drawLine(a.x, a.y, b.x, b.y, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        temp.drawCircle(a.x, a.y, 30, paint);
        temp.drawCircle(b.x, b.y, 30, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                mWidth = width;
                mHeight = width * mRatioHeight / mRatioWidth;
            } else {
                mWidth = height * mRatioWidth / mRatioHeight;
                mHeight = height;
            }
        }

        setMeasuredDimension(mWidth, mHeight);

        mRatioX = ((float) mImgWidth) / mWidth;
        mRatioY = ((float) mImgHeight) / mHeight;
    }
}

