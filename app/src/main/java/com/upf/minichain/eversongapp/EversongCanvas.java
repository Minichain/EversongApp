package com.upf.minichain.eversongapp;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;

import com.upf.minichain.eversongapp.enums.ChartTab;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class EversongCanvas {
    private Canvas mCanvas;
    private Paint mPaint01 = new Paint();
    private Paint mPaint02 = new Paint();
    private Paint chromagramPaint = new Paint();
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Rect mRect = new Rect();
    private int mColorBackground;
    private int mColor01;
    private int mColor02;

    private Shader shader1;

    public EversongCanvas(Resources resources, View imageView, final int canvasWidth, final int canvasHeight) {
        mColorBackground = ResourcesCompat.getColor(resources, R.color.colorBackgroundDark, null);
        mColor01 = ResourcesCompat.getColor(resources, R.color.colorWhite, null);
        mColor02 = ResourcesCompat.getColor(resources, R.color.colorRed, null);

        @SuppressLint("ResourceType")
        int[] color01RGB = Utils.hexadecimalToRgb(resources.getString(R.color.colorBackgroundDark));
        @SuppressLint("ResourceType")
        int[] color02RGB = Utils.hexadecimalToRgb(resources.getString(R.color.colorRed));

        shader1 = new LinearGradient(0, 400, 0, 500, Color.rgb(color01RGB[0], color01RGB[1], color01RGB[2]),
                Color.rgb(color02RGB[0], color02RGB[1], color02RGB[2]), Shader.TileMode.CLAMP);

        mPaint01.setColor(mColor01);
        mPaint01.setStrokeWidth(7.5f);
        mPaint01.setStyle(Paint.Style.STROKE);
        mPaint02.setColor(mColor02);
        mPaint02.setStrokeWidth(5f);
        chromagramPaint.setColor(mColor01);
        mImageView = (ImageView) imageView;
        mImageView.post( new Runnable() {   // Whenever the view is loaded...
            @Override
            public void run() {    //...this is run
//                Log.v(LOG_TAG, "EversongCanvasLog:: drawInCanvas with size: " + vWidth + ", " + vHeight);
                if (canvasWidth > 0 && canvasHeight > 0) {
                    mBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
                    mImageView.setImageBitmap(mBitmap);
                    mCanvas = new Canvas(mBitmap);
                    mCanvas.drawColor(mColorBackground);
                }
            }
        });
    }

    public void updateCanvas(final double[] bufferSamples, final double[] bufferFrequency, final double spectrumAverage,
                             final float pitch, final double[] chromagram, final int[] chordDetected) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas.drawColor(mColorBackground);    //Reset background color

        drawBufferSamples(bufferSamples, spectrumAverage, mPaint01);

        if (bufferSamples != null && Parameters.getChartTabSelected() == ChartTab.CHROMAGRAM) {
            drawChromagram(chromagram, spectrumAverage, chordDetected, chromagramPaint, mPaint02);
        }

        if (bufferFrequency != null && Parameters.isDebugMode()) {
            drawSpectrum(bufferFrequency, spectrumAverage, pitch, mPaint01);
        }
    }

    public void drawBufferSamples(double[] bufferSamples, double spectrumAverage, Paint paint) {
        float amplifyDrawFactor = 0;
        if (spectrumAverage > 0) {
            amplifyDrawFactor = 100f * 0.025f / (float)spectrumAverage;
        }
        double smoothEffectValue;
        int numberOfSamplesToPaint;
        int firstSampleToPaint;
        int verticalDisplacement = (mCanvas.getHeight() / 2);

        if (mCanvas.getWidth() < bufferSamples.length) {
            numberOfSamplesToPaint = mCanvas.getWidth();
        } else {
            numberOfSamplesToPaint = bufferSamples.length - 1;
        }
        firstSampleToPaint = (bufferSamples.length / 2) - (numberOfSamplesToPaint / 2);

        Path path = new Path();
        int step = 16;
        float x;
        float y;
        for (int i = firstSampleToPaint; i < numberOfSamplesToPaint + firstSampleToPaint; i = i + step) {
            smoothEffectValue = (0.5 * (1.0 - Math.cos(2.0 * Math.PI * (double)(i - firstSampleToPaint) / (double)(numberOfSamplesToPaint - 1))));
            x = i - firstSampleToPaint;
            y = (float) (bufferSamples[i] * smoothEffectValue * amplifyDrawFactor) + verticalDisplacement;
            if (i == firstSampleToPaint) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        mCanvas.drawPath(path, paint);
    }

    public void drawSpectrum(double[] spectrumBuffer, double spectrumAverage, float pitch, Paint paint) {
        mPaint02.setShader(shader1);
        if (pitch > 0) {    //Pitch
            int pitchIndex = AudioStack.getIndexByFrequency(pitch);
            mCanvas.drawLine(pitchIndex, 0, pitchIndex, mCanvas.getHeight(), mPaint02);
        }

        float amplifyDrawFactor = 1000f * 0.00025f / (float)spectrumAverage;
        Path path = new Path();
        int step = 8;
        float x;
        float y;
        for (int i = 0; i < (spectrumBuffer.length / 2) - 1; i = i + step) {
            x = i;
            y = ((float)spectrumBuffer[i] * (-amplifyDrawFactor)) + (mCanvas.getHeight());
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        mCanvas.drawPath(path, paint);

        //Spectrum average
        spectrumAverage = (spectrumAverage * -amplifyDrawFactor) + mCanvas.getHeight();
        mCanvas.drawLine(0, (float)spectrumAverage, mCanvas.getWidth(), (float)spectrumAverage, mPaint01);
    }

    public void drawChromagram(double[] chromagram, double spectrumAverage, int[] chordDetected, Paint paint01, Paint paint02) {
        int notesBins = (int)((float)mCanvas.getWidth() / 12f);
        int paddingBetweenBins = 10;
        int bottomPadding = 300;
        paint01.setTextSize(45);

        float amplifyDrawFactor = 0;
        if (spectrumAverage > 0) {
            amplifyDrawFactor = 0.50f * 0.00025f / (float)spectrumAverage;
        }

        for (int i = 0; i < NotesEnum.numberOfNotes; i++) {
            mRect.set(i * notesBins, (mCanvas.getHeight() - bottomPadding) - (int)(chromagram[i] * amplifyDrawFactor * (double)mCanvas.getHeight()),
                    i * notesBins + (notesBins - paddingBetweenBins), mCanvas.getHeight() - bottomPadding);
            boolean noteIsInTheChord = false;
            NotesEnum[] chordNotes = Utils.getChordNotes(NotesEnum.fromInteger(chordDetected[0]), ChordTypeEnum.fromInteger(chordDetected[1]));
            for (int z = 0; z < 4; z++) {
                if (chordNotes[z].toString().equals(NotesEnum.fromInteger(i).toString())) {
                    noteIsInTheChord = true;
                }
            }
            if (noteIsInTheChord) {
                mCanvas.drawRect(mRect, paint02);
            } else {
                mCanvas.drawRect(mRect, paint01);
            }
            mCanvas.drawText(NotesEnum.fromInteger(i).toString(), i * notesBins, mCanvas.getHeight() - bottomPadding + 40, paint01);
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }
}
