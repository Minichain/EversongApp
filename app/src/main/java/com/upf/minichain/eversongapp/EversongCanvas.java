package com.upf.minichain.eversongapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;

public class EversongCanvas {
    private Canvas mCanvas;
    private Paint mPaint01 = new Paint();
    private Paint mPaint02 = new Paint();
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Rect mRect = new Rect();
    private int mColorBackground;
    private int mColor01;
    private int mColor02;

    private Shader shader1;

    public EversongCanvas(Resources resources, View imageView) {
        mColorBackground = ResourcesCompat.getColor(resources, R.color.colorBackground, null);
        mColor01 = ResourcesCompat.getColor(resources, R.color.mColor01, null);
        mColor02 = ResourcesCompat.getColor(resources, R.color.mColor02, null);

        int[] color01RGB = hexadecimalToRgb(resources.getString(R.color.colorBackground));
        int[] color02RGB = hexadecimalToRgb(resources.getString(R.color.mColor02));
        shader1 = new LinearGradient(0, 400, 0, 500, Color.rgb(color01RGB[0], color01RGB[1], color01RGB[2]),
                Color.rgb(color02RGB[0], color02RGB[1], color02RGB[2]), Shader.TileMode.CLAMP);

        mPaint01.setColor(mColorBackground);
        mImageView = (ImageView) imageView;
        mImageView.post( new Runnable() {   // Whenever the view is loaded...
            @Override
            public void run() {    //...this is run
                int vWidth = mImageView.getWidth();
                int vHeight = mImageView.getHeight();
//                Log.v(LOG_TAG, "EversongCanvasLog:: drawInCanvas with size: " + vWidth + ", " + vHeight);
                if (vWidth > 0 && vHeight > 0) {
                    mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
                    mImageView.setImageBitmap(mBitmap);
                    mCanvas = new Canvas(mBitmap);
                    mCanvas.drawColor(mColorBackground);
                }
            }
        });
    }

    public void updateCanvas(short[] buffer, double[] bufferFrequency, double average, float pitch) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas.drawColor(mColorBackground);    //Reset background color
        mPaint01.setColor(mColor01);
        mPaint01.setStrokeWidth(5f);
        mPaint02.setColor(mColor02);
        mPaint02.setStrokeWidth(5f);

        if (buffer != null && bufferFrequency != null) {
            mPaint02.setShader(shader1);
            if (pitch > 0) {
                int pitchIndex = AudioStack.getIndexByFrequency(pitch);
                //Pitch
                mCanvas.drawLine(pitchIndex, 0, pitchIndex, mCanvas.getHeight(), mPaint02);
            }

            int rectangleWidth = (int) ((mImageView.getWidth() - 10) * average) + 10;
            mRect.set(10, 10, rectangleWidth,100);
            mCanvas.drawRect(mRect, mPaint01);

            float amplifyDrawFactor = 500f;
            for (int i = 0; i < (bufferFrequency.length / 2) - 1; i++) {
                //Time domain
                mCanvas.drawLine(i, (buffer[i] / 60) + (mCanvas.getHeight() / 2),
                        i + 1 , (buffer[i + 1] / 60) + (mCanvas.getHeight() / 2), mPaint01);

                //Frequency domain
                mCanvas.drawLine(i, ((float)bufferFrequency[i] * (-amplifyDrawFactor)) + (mCanvas.getHeight()),
                        i + 1, ((float)bufferFrequency[(i) + 1] * (-amplifyDrawFactor)) + (mCanvas.getHeight()), mPaint01);

            }
            average = (average * -amplifyDrawFactor) + mCanvas.getHeight();
            //Spectrum average
            mCanvas.drawLine(0, (float)average, mCanvas.getWidth(), (float)average, mPaint01);
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    public static int[] hexadecimalToRgb(String hex) {
        hex = hex.replace("#", "");
        if (hex.length() == 8) {
            hex = hex.substring(2, 8);  //Transforms "FCFCFCFC" into "FCFCFC"
        }
        final int[] ret = new int[3];
        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }
}
