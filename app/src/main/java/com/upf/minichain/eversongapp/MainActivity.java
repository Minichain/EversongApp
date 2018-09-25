package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    boolean mShouldContinue;        // Indicates if recording / playback should stop
    Button recordingButton;
    TextView frequencyText;
    TextView noteText;

    //Canvas variables:
    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Rect mRect = new Rect();
    private int mColorBackground;
    private int mColor01;
    private int mColor02;
    private int mColorBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkCaptureAudioPermission();

        recordingButton = this.findViewById(R.id.recording_button);
        frequencyText = this.findViewById(R.id.frequency_text);
        noteText = this.findViewById(R.id.note_text);
        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.l("AdriHell:: Button pressed! recordingButton is " + recordingButton);
                if (recordingButton.getText().equals(getString(R.string.start_record_button))) {
                    recordingButton.setText(R.string.stop_record_button);
                    recordAudio();
                } else if (recordingButton.getText().equals(getString(R.string.stop_record_button))) {
                    recordingButton.setText(R.string.start_record_button);
                    mShouldContinue = false;
                }
            }
        });

        initCanvas();
    }

    //--------------------- Canvas code ---------------------------
    public void initCanvas() {
        mColorBackground = ResourcesCompat.getColor(getResources(),
                R.color.colorBackground, null);
        mColor01 = ResourcesCompat.getColor(getResources(),
                R.color.mColor01, null);
        mColor02 = ResourcesCompat.getColor(getResources(),
                R.color.mColor02, null);
        mColorBlack = ResourcesCompat.getColor(getResources(),
                R.color.colorBlack, null);

        mPaint.setColor(mColorBackground);
        mImageView = this.findViewById(R.id.canvas_view);
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

    public void updateCanvas(short[] buffer, double[] bufferFrequency) {
        mImageView.setImageBitmap(mBitmap);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mColorBackground);
        mPaint.setColor(mColorBlack);
        mPaint.setStrokeWidth(5f);
        mCanvas.drawRect(mRect, mPaint);

        if (buffer != null && bufferFrequency != null) {
            for (int i = 0; i < (bufferFrequency.length / 2) - 1; i++) {

                mCanvas.drawLine(i * 2, (buffer[i] / 60) + (mCanvas.getHeight() / 2),
                        i * 2 + 1 , (buffer[i + 1] / 60) + (mCanvas.getHeight() / 2), mPaint);

                mCanvas.drawLine(i * 2, ((float)Math.abs(bufferFrequency[i]) * -500) + (mCanvas.getHeight()),
                        i * 2 + 1, ((float)Math.abs(bufferFrequency[(i) + 1]) * -500) + (mCanvas.getHeight()), mPaint);

                int averageValueY = (int)(AudioUtils.getAverageLevel(bufferFrequency) * -20000 + mCanvas.getHeight());
                mCanvas.drawLine(0, averageValueY, mCanvas.getWidth(), averageValueY, mPaint);
            }
        }
    }
    //--------------------- Canvas code ---------------------------

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processAudio(short[] buffer, double[] bufferFrequency) {
        NoteDetector noteDetector = new NoteDetector();
        float freqDetected = noteDetector.detectFrequency(bufferFrequency);
//        frequencyText.setText(String.valueOf((int)freqDetected + " Hz"));
//        noteText.setText(String.valueOf(noteDetector.detectNote(freqDetected)));
        int[] peaks;
        peaks = noteDetector.detectPeaks(bufferFrequency, 5, AudioUtils.getAverageLevel(bufferFrequency) * 40);
        Log.l("AdriHell:: " + peaks[0]
                + ", " + peaks[1]
                + ", " + peaks[2]
                + ", " + peaks[3]
                + ", " + peaks[4]);
        Log.l("AdriHell:: " + noteDetector.indexToFrequency(peaks[0])
                + ", " + noteDetector.indexToFrequency(peaks[1])
                + ", " + noteDetector.indexToFrequency(peaks[2])
                + ", " + noteDetector.indexToFrequency(peaks[3])
                + ", " + noteDetector.indexToFrequency(peaks[4]));
    }

    void recordAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                    bufferSize = SAMPLE_RATE * 2;
                }
                bufferSize = Constants.BUFFER_SIZE;

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.l("AdriHell:: Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.l("AdriHell:: Start recording");

                long shortsRead = 0;
                mShouldContinue = true;

                while (mShouldContinue) {
                    final short[] audioBuffer = new short[bufferSize / 2];
                    final short MAX_SHORT_VALUE = 32767;
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;

                    final double[] audioBufferDouble = new double[bufferSize / 2];
                    for (int i = 0; i < audioBuffer.length;  i++) {
                        audioBufferDouble[i] = (double)audioBuffer[i] / (double)MAX_SHORT_VALUE;
                    }
                    final double[] audioBufferFrequency = AudioUtils.smoothFunction(AudioUtils.bandPassFilter(AudioUtils.fft(audioBufferDouble, true), 150, 2000));

                    runOnUiThread(new Runnable() {
                        final int amplitudePercentage = (int) Math.abs(((float)audioBuffer[0] / (float)MAX_SHORT_VALUE) * 100.0);
                        @Override
                        public void run() {
                            if (mCanvas != null && mPaint != null) {
                                int rectangleWidth = (int) ((float) (mImageView.getWidth() - 10) * (Math.abs(amplitudePercentage) / 100.0)) + 10;
//                                Log.v(LOG_TAG, "AdriHell:: setting rectangle to size: " + rectangleWidth);
                                mRect.set(10, 10, rectangleWidth,100);
                                updateCanvas(audioBuffer, audioBufferFrequency);
                                processAudio(audioBuffer, audioBufferFrequency);
                            }
                        }
                    });
//                    Log.v(LOG_TAG, "AdriHell:: reading buffer of size " + bufferSize);
                }

                record.stop();
                record.release();

                Log.l("AdriHell:: Recording stopped. Samples read: " + shortsRead);
            }
        }).start();
    }
}