package com.upf.minichain.eversongapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;

public class EversongCanvas {
    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Rect mRect = new Rect();
    private int mColorBackground;
    private int mColor01;
    private int mColor02;
    private int mColorBlack;


    public EversongCanvas(Resources resources, View imageView) {
        mColorBackground = ResourcesCompat.getColor(resources, R.color.colorBackground, null);
        mColor01 = ResourcesCompat.getColor(resources, R.color.mColor01, null);
        mColor02 = ResourcesCompat.getColor(resources, R.color.mColor02, null);
        mColorBlack = ResourcesCompat.getColor(resources, R.color.colorBlack, null);

        mPaint.setColor(mColorBackground);
        mImageView = (ImageView) imageView;
        mImageView.post( new Runnable() {   // Whenever the view is loaded...
            @Override
            public void run() {    //...this is run
                int vWidth = mImageView.getWidth();
                int vHeight = mImageView.getHeight();
//                Log.v(LOG_TAG, "AdriHell:: drawInCanvas with size: " + vWidth + ", " + vHeight);
                if (vWidth > 0 && vHeight > 0) {
                    mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
                    mImageView.setImageBitmap(mBitmap);
                    mCanvas = new Canvas(mBitmap);
                }
            }
        });
    }

    public void updateCanvas(short[] buffer, double[] bufferFrequency, double average) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mColorBackground);
        mPaint.setColor(mColorBlack);
        mPaint.setStrokeWidth(5f);

        int rectangleWidth = (int) ((mImageView.getWidth() - 10) * average) + 10;
        mRect.set(10, 10, rectangleWidth,100);
        mCanvas.drawRect(mRect, mPaint);

        if (buffer != null && bufferFrequency != null) {
            for (int i = 0; i < (bufferFrequency.length / 2) - 1; i++) {

                mCanvas.drawLine(i * 2, (buffer[i] / 60) + (mCanvas.getHeight() / 2),
                        i * 2 + 1 , (buffer[i + 1] / 60) + (mCanvas.getHeight() / 2), mPaint);

                float amplifyDrawFactor = 500f;
                mCanvas.drawLine(i * 2, ((float)bufferFrequency[i] * (-amplifyDrawFactor)) + (mCanvas.getHeight()),
                        i * 2 + 1, ((float)bufferFrequency[(i) + 1] * (-amplifyDrawFactor)) + (mCanvas.getHeight()), mPaint);

                average = (average * -amplifyDrawFactor) + mCanvas.getHeight();
                mCanvas.drawLine(0, (float)average, mCanvas.getWidth(), (float)average, mPaint);
            }
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }
}
