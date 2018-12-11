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
import android.widget.ImageView;
import android.widget.TextView;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class MainActivity extends AppCompatActivity {
    boolean mShouldContinue;        // Indicates if recording / playback should stop
    Button recordingButton;
    TextView pitchText;
    TextView chordNoteText;
    TextView mostProbableChordNoteText;
    TextView chordTypeText;
    TextView mostProbableChordTypeText;
    EversongCanvas canvas;

    float pitchDetected;
    NotesEnum pitchNote;
    int[] chordDetected = new int[2];
    int[][] chordsDetectedBuffer;
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
        pitchNote = NotesEnum.NO_NOTE;
        chordDetected[0] = -1;
        chordDetected[1] = -1;
        mostProbableChord[0] = -1;
        mostProbableChord[1] = -1;
        chordsDetectedBuffer = new int[Parameters.getInstance().getChordBufferSize()][2];

        recordingButton = this.findViewById(R.id.recording_button);

        int color  = ResourcesCompat.getColor(getResources(), R.color.mColor01, null);

        if (BuildConfig.FLAVOR.equals("dev")) {
            chordNoteText = this.findViewById(R.id.chord_note);
            chordTypeText = this.findViewById(R.id.chord_type);
            pitchText = this.findViewById(R.id.pitch_text);

            chordNoteText.setTextColor(color);
            chordTypeText.setTextColor(color);
            pitchText.setTextColor(color);
        }

        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);

        mostProbableChordNoteText.setTextColor(color);
        mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.A));
        mostProbableChordTypeText.setTextColor(color);
        mostProbableChordTypeText.setText(ChordTypeEnum.getString(ChordTypeEnum.Major));

        canvas =  new EversongCanvas(getResources(), this.findViewById(R.id.canvas_view));

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.l("MainActivityLog:: recordingButton pressed!");
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

    private void setChordChart(NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] guitarChordChart = GuitarChordChart.getChordTab(tonic, chordType);

        ImageView guitarChordStringView;
        int numberOfStrings = 6;

        for (int i = 1; i <= numberOfStrings; i++) {
            int guitarChordStringViewId = getResources().getIdentifier("guitar_chord_string_0" + i, "id", getPackageName());
            guitarChordStringView = this.findViewById(guitarChordStringViewId);
            guitarChordStringView.setAlpha(alpha);
            int guitarChordStringImageId;
            if (guitarChordChart[numberOfStrings - i] == -1) {
                guitarChordStringImageId = getResources().getIdentifier("guitar_chord_string_0", "drawable", getPackageName());
            } else {
                guitarChordStringImageId = getResources().getIdentifier("guitar_chord_string_0" + guitarChordChart[numberOfStrings - i], "drawable", getPackageName());
            }
            guitarChordStringView.setImageResource(guitarChordStringImageId);
        }
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processAudio(final double[] bufferDouble, final double[] bufferFrequency, final double average) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int[] chordDetectedThread = AudioStack.chordDetection(bufferDouble, bufferFrequency);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chordDetected = chordDetectedThread;
                        chordsDetectedBuffer[chordBufferIterator % Parameters.getInstance().getChordBufferSize()][0] = chordDetected[0];
                        chordsDetectedBuffer[chordBufferIterator % Parameters.getInstance().getChordBufferSize()][1] = chordDetected[1];
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
//        Log.l("MainActivityLog:: mostProbableChord: "+ NotesEnum.getString(NotesEnum.fromInteger(mostProbableChord[0]))
//                + String.valueOf(ChordTypeEnum.fromInteger(mostProbableChord[1])) + ". Probability: " + mostProbableChord[2] + "%");

        if (canvas.getCanvas() != null) {
            canvas.updateCanvas(bufferDouble, bufferFrequency, average, pitchDetected);
        }

        if (BuildConfig.FLAVOR.equals("dev")) {
            if (pitchDetected != -1) {
                pitchNote = AudioStack.getNoteByFrequency((double)pitchDetected);
                pitchText.setText(String.valueOf("Pitch: \n" + (int)pitchDetected + " Hz" + " ("
                        + NotesEnum.getString(pitchNote) + ")"));
            } else {
                pitchNote = NotesEnum.NO_NOTE;
                pitchText.setText(String.valueOf("Pitch: \n" + NotesEnum.getString(pitchNote) + " Hz"));
            }

            if (chordDetected[0] != -1) {
                chordNoteText.setText(NotesEnum.getString(NotesEnum.fromInteger(chordDetected[0])));
            } else {
                chordNoteText.setText(NotesEnum.getString(NotesEnum.NO_NOTE));
            }

            if (chordDetected[1] != -1) {
                chordTypeText.setText(ChordTypeEnum.getString(ChordTypeEnum.fromInteger(chordDetected[1])));
            } else {
                chordTypeText.setText(ChordTypeEnum.getString(ChordTypeEnum.Other));
            }
        }

        if (mostProbableChord[0] != -1 && mostProbableChord[1] != -1) {
            mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.fromInteger(mostProbableChord[0])));
            mostProbableChordNoteText.setAlpha((float)mostProbableChord[2] / 100f);
            mostProbableChordTypeText.setText(ChordTypeEnum.getString(ChordTypeEnum.fromInteger(mostProbableChord[1])));
            mostProbableChordTypeText.setAlpha((float)mostProbableChord[2] / 100f);

            setChordChart(NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f);
        } else {
            mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.NO_NOTE));
            mostProbableChordTypeText.setText(ChordTypeEnum.getString(ChordTypeEnum.Other));
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
                    Log.l("MainActivityLog:: Audio Record cannot be initialized!");
                    return;
                }
                record.startRecording();

                Log.l("MainActivityLog:: Start recording");

                long shortsRead = 0;
                mShouldContinue = true;

                while (mShouldContinue) {
                    final short[] audioBuffer = new short[bufferSize];
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;
                    final double[] audioBufferDouble = AudioStack.window(AudioStack.getSamplesToDouble(audioBuffer), Parameters.getInstance().getWindowingFunction());
                    final double[] audioBufferFrequency = AudioStack.bandPassFilter(AudioStack.fft(audioBufferDouble, true), 20, 8000);
                    final double average = AudioStack.getAverageLevel(audioBufferFrequency) * 25;
//                    Log.l("MainActivityLog:: Average level " + average);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processAudio(audioBufferDouble, audioBufferFrequency, average);
                        }
                    });
//                    Log.l("MainActivityLog:: reading buffer of size " + bufferSize);
                }

                record.stop();
                record.release();

                Log.l("MainActivityLog:: Recording stopped. Num of samples read: " + shortsRead);
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
                this.onPause();
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