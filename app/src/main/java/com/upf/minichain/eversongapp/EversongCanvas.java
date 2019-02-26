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

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
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

        int[] color01RGB = Utils.hexadecimalToRgb(resources.getString(R.color.colorBackground));
        int[] color02RGB = Utils.hexadecimalToRgb(resources.getString(R.color.mColor02));
        shader1 = new LinearGradient(0, 400, 0, 500, Color.rgb(color01RGB[0], color01RGB[1], color01RGB[2]),
                Color.rgb(color02RGB[0], color02RGB[1], color02RGB[2]), Shader.TileMode.CLAMP);

        mPaint01.setColor(mColor01);
        mPaint01.setStrokeWidth(5f);
        mPaint02.setColor(mColor02);
        mPaint02.setStrokeWidth(5f);
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

    public void updateCanvas(final double[] bufferSamples, final double[] bufferFrequency, final double spectrumAverage, final float pitch, final double[] chromagram, final int[] chordDetected) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas.drawColor(mColorBackground);    //Reset background color

        if (bufferSamples != null && bufferFrequency != null) {
            drawBufferSamples(bufferSamples, spectrumAverage, mPaint01);
            switch(Parameters.getInstance().getChartTabSelected()) {
                case GUITAR_TAB:
                case UKULELE_TAB:
                case PIANO_TAB:
                case STAFF_TAB:
                    break;
                case CHROMAGRAM:
                    drawChromagram(chromagram, spectrumAverage, chordDetected, mPaint01, mPaint02);
                    break;
            }
            if (Parameters.getInstance().isDebugMode()) {
                drawSpectrum(bufferFrequency, spectrumAverage, pitch, mPaint01);
            }
        }
    }

    public void drawBufferSamples(double[] bufferSamples, double spectrumAverage, Paint paint) {
        float amplifyDrawFactor = 100f * 0.00025f / (float)spectrumAverage;
        double smoothEffectValue;
        int numberOfSamplesToPaint;
        int firstSampleToPaint;

        if (mCanvas.getWidth() < bufferSamples.length) {
            numberOfSamplesToPaint = mCanvas.getWidth();
        } else {
            numberOfSamplesToPaint = bufferSamples.length - 1;
        }
        firstSampleToPaint = (bufferSamples.length / 2) - (numberOfSamplesToPaint / 2);

        for (int i = firstSampleToPaint; i < numberOfSamplesToPaint + firstSampleToPaint; i++) {
            smoothEffectValue = (0.5 * (1.0 - Math.cos(2.0*Math.PI*(double)(i - firstSampleToPaint)/(double)(mCanvas.getWidth() - 1))));
            mCanvas.drawLine(i - firstSampleToPaint, (float) (bufferSamples[i] * 100 * smoothEffectValue * amplifyDrawFactor) + (mCanvas.getHeight() / 2),
                    i + 1 - firstSampleToPaint, (float) (bufferSamples[i + 1] * 100 * smoothEffectValue * amplifyDrawFactor) + (mCanvas.getHeight() / 2), paint);
        }
    }

    public void drawSpectrum(double[] spectrumBuffer, double spectrumAverage, float pitch, Paint paint) {
        mPaint02.setShader(shader1);
        if (pitch > 0) {    //Pitch
            int pitchIndex = AudioStack.getIndexByFrequency(pitch);
            mCanvas.drawLine(pitchIndex, 0, pitchIndex, mCanvas.getHeight(), mPaint02);
        }

        float amplifyDrawFactor = 1000f * 0.00025f / (float)spectrumAverage;
        for (int i = 0; i < (spectrumBuffer.length / 2) - 1; i++) {
            mCanvas.drawLine(i, ((float)spectrumBuffer[i] * (-amplifyDrawFactor)) + (mCanvas.getHeight()),
                    i + 1, ((float)spectrumBuffer[(i) + 1] * (-amplifyDrawFactor)) + (mCanvas.getHeight()), paint);
        }
        spectrumAverage = (spectrumAverage * -amplifyDrawFactor) + mCanvas.getHeight();
        //Spectrum average
        mCanvas.drawLine(0, (float)spectrumAverage, mCanvas.getWidth(), (float)spectrumAverage, mPaint01);
    }

    public void drawChromagram(double[] chromagram, double spectrumAverage, int[] chordDetected, Paint paint01, Paint paint02) {
        int notesBins = (int)((float)mCanvas.getWidth() / 12f);
        int paddingBetweenBins = 10;
        int bottomPadding = 300;
        paint01.setTextSize(45);

        float amplifyDrawFactor = 0.50f * 0.00025f / (float)spectrumAverage;
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
