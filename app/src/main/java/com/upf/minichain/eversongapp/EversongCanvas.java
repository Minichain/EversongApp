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

import com.upf.minichain.eversongapp.enums.NotesEnum;

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

    public void updateCanvas(double[] bufferSamples, double[] bufferFrequency, double spectrumAverage, float pitch, double[] chromagram) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas.drawColor(mColorBackground);    //Reset background color
        mPaint01.setColor(mColor01);
        mPaint01.setStrokeWidth(5f);
        mPaint02.setColor(mColor02);
        mPaint02.setStrokeWidth(5f);

        if (bufferSamples != null && bufferFrequency != null) {
            mPaint02.setShader(shader1);
            if (pitch > 0) {
                int pitchIndex = AudioStack.getIndexByFrequency(pitch);
                //Pitch
                mCanvas.drawLine(pitchIndex, 0, pitchIndex, mCanvas.getHeight(), mPaint02);
            }

            int rectangleWidth = (int) ((mImageView.getWidth() - 10) * spectrumAverage) + 10;
            mRect.set(10, 10, rectangleWidth,100);
            mCanvas.drawRect(mRect, mPaint01);

            drawBufferSamples(bufferSamples, mPaint01);
            drawSpectrum(bufferFrequency, spectrumAverage, mPaint01);
//            drawChromagram(chromagram, mPaint01);
        }
    }

    public void drawBufferSamples(double[] bufferSamples, Paint paint) {
        float amplifyDrawFactor = 250f;
        for (int i = 0; i < bufferSamples.length - 1; i++) {
            double smoothEffectValue = (0.5 * (1.0 - Math.cos(2.0*Math.PI*(double)i/(double)(mCanvas.getWidth() - 1))));
            mCanvas.drawLine(i, (float) (bufferSamples[i] * 100 * smoothEffectValue * amplifyDrawFactor) + (mCanvas.getHeight() / 2),
                    i + 1, (float) (bufferSamples[i + 1] * 100 * smoothEffectValue * amplifyDrawFactor) + (mCanvas.getHeight() / 2), paint);
        }
    }

    public void drawSpectrum(double[] spectrumBuffer, double spectrumAverage, Paint paint) {
        float amplifyDrawFactor = 500f;
        for (int i = 0; i < (spectrumBuffer.length / 2) - 1; i++) {
            mCanvas.drawLine(i, ((float)spectrumBuffer[i] * (-amplifyDrawFactor)) + (mCanvas.getHeight()),
                    i + 1, ((float)spectrumBuffer[(i) + 1] * (-amplifyDrawFactor)) + (mCanvas.getHeight()), paint);
        }
        spectrumAverage = (spectrumAverage * -amplifyDrawFactor) + mCanvas.getHeight();
        //Spectrum average
        mCanvas.drawLine(0, (float)spectrumAverage, mCanvas.getWidth(), (float)spectrumAverage, mPaint01);
    }

    public void drawChromagram(double[] chromagram, Paint paint) {
        int notesBins = (int)((float)mCanvas.getWidth() / 12f);
        int paddingBetweenBins = 10;
        int bottomPadding = 300;
        paint.setTextSize(45);

        for (int i = 0; i < NotesEnum.numberOfNotes; i++) {
            mRect.set(i * notesBins, (mCanvas.getHeight() - bottomPadding) - (int)(chromagram[i] * (double)mCanvas.getHeight()),
                    i * notesBins + (notesBins - paddingBetweenBins), mCanvas.getHeight() - bottomPadding);
            mCanvas.drawRect(mRect, paint);
            mCanvas.drawText(NotesEnum.getString(NotesEnum.fromInteger(i)), i * notesBins, mCanvas.getHeight() - bottomPadding + 40, paint);
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
