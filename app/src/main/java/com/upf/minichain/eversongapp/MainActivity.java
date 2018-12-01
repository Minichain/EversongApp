package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class MainActivity extends AppCompatActivity {
    boolean mShouldContinue;        // Indicates if recording / playback should stop
    Button recordingButton;
    TextView frequencyText;
    TextView chordNoteText;
    TextView mostProbableChordNoteText;
    TextView chordTypeText;
    TextView mostProbableChordTypeText;
    EversongCanvas canvas;

    float pitchDetected;
    int[] chordDetected = new int[2];
    int[][] chordsDetectedBuffer = new int[Parameters.CHORD_BUFFER_SIZE][2];
    int chordBufferIterator = 0;
    int[] mostProbableChord = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCaptureAudioPermission();
        initMainActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        mShouldContinue = false;
        super.onPause();
    }

    public void initMainActivity() {
        AudioStack.initAudioStack();

        pitchDetected = -1;
        chordDetected[0] = -1;
        chordDetected[1] = -1;
        mostProbableChord[0] = -1;
        mostProbableChord[1] = -1;

        recordingButton = this.findViewById(R.id.recording_button);

        frequencyText = this.findViewById(R.id.frequency_text);
        chordNoteText = this.findViewById(R.id.chord_note);
        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        chordTypeText = this.findViewById(R.id.chord_type);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);

        int color  = ResourcesCompat.getColor(getResources(), R.color.mColor01, null);
        frequencyText.setTextColor(color);
        chordNoteText.setTextColor(color);
        chordTypeText.setTextColor(color);
        mostProbableChordNoteText.setTextColor(color);
        mostProbableChordTypeText.setTextColor(color);

        canvas =  new EversongCanvas(getResources(), this.findViewById(R.id.canvas_view));

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.l("AdriHell:: recordingButton pressed!");
                if (recordingButton.getText().equals(getString(R.string.start_record_button))) {
                    recordingButton.setText(R.string.stop_record_button);
                    recordAudio();
                } else if (recordingButton.getText().equals(getString(R.string.stop_record_button))) {
                    recordingButton.setText(R.string.start_record_button);
                    mShouldContinue = false;
                }
            }
        });
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processAudio(final short[] bufferShort, final double[] bufferDouble, final double[] bufferFrequency, final double average) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int[] chordDetectedThread = AudioStack.chordDetection(bufferDouble, bufferFrequency);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chordDetected = chordDetectedThread;
                        chordsDetectedBuffer[chordBufferIterator % Parameters.CHORD_BUFFER_SIZE][0] = chordDetected[0];
                        chordsDetectedBuffer[chordBufferIterator % Parameters.CHORD_BUFFER_SIZE][1] = chordDetected[1];
                        chordBufferIterator++;
                    }
                });
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final float pitchDetectedThread = AudioStack.getPitch(bufferDouble);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitchDetected = pitchDetectedThread;
                    }
                });
            }
        }).start();

        mostProbableChord = AudioStack.getMostProbableChord(chordsDetectedBuffer);
//        Log.l("AdriHell:: mostProbableChord: "+ NotesEnum.getString(NotesEnum.fromInteger(mostProbableChord[0]))
//                + String.valueOf(ChordTypeEnum.fromInteger(mostProbableChord[1])) + ". Probability: " + mostProbableChord[2] + "%");

        if (canvas.getCanvas() != null) {
            canvas.updateCanvas(bufferShort, bufferFrequency, average, pitchDetected);
        }

        if (pitchDetected != -1) {
            frequencyText.setText(String.valueOf("Pitch: \n" + (int)pitchDetected + " Hz"));
        } else {
            frequencyText.setText(String.valueOf("Pitch: \n" + NotesEnum.getString(NotesEnum.NO_NOTE) + " Hz"));
        }
        if (chordDetected[0] != -1) {
            chordNoteText.setText(NotesEnum.getString(NotesEnum.fromInteger(chordDetected[0])));
        } else {
            chordNoteText.setText(NotesEnum.getString(NotesEnum.NO_NOTE));
        }
        if (chordDetected[1] != -1) {
            chordTypeText.setText(String.valueOf(ChordTypeEnum.fromInteger(chordDetected[1])));
        } else {
            chordTypeText.setText(String.valueOf(ChordTypeEnum.Other));
        }
        if (mostProbableChord[0] != -1) {
            mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.fromInteger(mostProbableChord[0])));
            mostProbableChordNoteText.setAlpha((float)mostProbableChord[2] / 100f);
        } else {
            mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.NO_NOTE));
        }
        if (mostProbableChord[1] != -1) {
            mostProbableChordTypeText.setText(String.valueOf(ChordTypeEnum.fromInteger(mostProbableChord[1])));
            mostProbableChordTypeText.setAlpha((float)mostProbableChord[2] / 100f);
        } else {
            mostProbableChordTypeText.setText(String.valueOf(ChordTypeEnum.Other));
        }
    }

    void recordAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(Parameters.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                    bufferSize = SAMPLE_RATE * 2;
                }
                bufferSize = Parameters.BUFFER_SIZE;

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        Parameters.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.l("AdriHell:: Audio Record cannot be initialized!");
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
                    final double[] audioBufferFrequency = AudioStack.bandPassFilter(AudioStack.fft(audioBufferDouble, true, Parameters.getInstance().getWindowingFunction()), 150, 4000);
                    final double average = AudioStack.getAverageLevel(audioBufferFrequency) * 25;
//                    Log.l("AdriHell:: Average level " + average);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processAudio(audioBuffer, audioBufferDouble, audioBufferFrequency, average);
                        }
                    });
//                    Log.l("AdriHell:: reading buffer of size " + bufferSize);
                }

                record.stop();
                record.release();

                Log.l("AdriHell:: Recording stopped. Num of samples read: " + shortsRead);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dropdown_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.open_settings_menu_option:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.exit_app_option:
                closeApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void closeApp() {
        this.finish();
        System.exit(0);
    }
}