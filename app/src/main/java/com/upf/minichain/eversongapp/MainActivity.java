package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    boolean mShouldContinue;        // Indicates if recording / playback should stop
    Button recordingButton;
    TextView frequencyText;
    TextView noteText;
    TextView chordTypeText;
    EversongCanvas canvas;

    float pitchDetected;
    int[] chordDetected = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkCaptureAudioPermission();
        AudioStack.initAudioStack();

        pitchDetected = -1;
        chordDetected[0] = -1;
        chordDetected[1] = -1;

        recordingButton = this.findViewById(R.id.recording_button);
        frequencyText = this.findViewById(R.id.frequency_text);
        noteText = this.findViewById(R.id.note_text);
        chordTypeText = this.findViewById(R.id.chord_type);
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

        canvas =  new EversongCanvas(getResources(), this.findViewById(R.id.canvas_view));
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processAudio(final double[] buffer, final double[] bufferFrequency, double average) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final int[] chordDetectedThread = AudioStack.chordDetection(buffer, bufferFrequency);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chordDetected = chordDetectedThread;
                    }
                });
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final float pitchDetectedThread = AudioStack.getPitch(buffer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitchDetected = pitchDetectedThread;
                    }
                });
            }
        }).start();

        if (pitchDetected != -1) {
            frequencyText.setText(String.valueOf("Pitch: \n" + (int)pitchDetected + " Hz"));
        } else {
            frequencyText.setText(String.valueOf("Pitch: \n" + NotesEnum.getString(NotesEnum.NO_NOTE) + " Hz"));
        }
        if (chordDetected[0] != -1) {
            noteText.setText(NotesEnum.getString(NotesEnum.fromInteger(chordDetected[0])));
        } else {
            noteText.setText(String.valueOf(NotesEnum.NO_NOTE));
        }
        if (chordDetected[1] != -1) {
            chordTypeText.setText(String.valueOf(ChordTypeEnum.fromInteger(chordDetected[1])));
        } else {
            chordTypeText.setText(String.valueOf(ChordTypeEnum.Other));
        }
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
                    final short[] audioBuffer = new short[bufferSize];
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;

                    final double[] audioBufferDouble = new double[audioBuffer.length];
                    for (int i = 0; i < audioBuffer.length;  i++) {
                        audioBufferDouble[i] = (double)audioBuffer[i] / (double)Short.MAX_VALUE;
                    }
//                    final double[] audioBufferFrequency = AudioStack.smoothFunction(AudioStack.bandPassFilter(AudioStack.fft(audioBufferDouble, true), 150, 2000));
                    final double[] audioBufferFrequency = AudioStack.bandPassFilter(AudioStack.fft(audioBufferDouble, true), 150, 4000);
                    final double average = AudioStack.getAverageLevel(audioBufferFrequency) * 25;
//                    Log.l("AdriHell:: Average level " + average);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (canvas.getCanvas() != null) {
                                canvas.updateCanvas(audioBuffer, audioBufferFrequency, average);
                                processAudio(audioBufferDouble, audioBufferFrequency, average);
                            }
                        }
                    });
//                    Log.l("AdriHell:: reading buffer of size " + bufferSize);
                }

                record.stop();
                record.release();

                Log.l("AdriHell:: Recording stopped. Samples read: " + shortsRead);
            }
        }).start();
    }
}