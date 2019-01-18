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
import android.widget.LinearLayout;
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
    float pitchProbability;
    NotesEnum pitchNote;
    int[] chordDetected = new int[2];
    int[][] chordsDetectedBuffer;
    int chordBufferIterator = 0;
    int[] mostProbableChord = new int[3];
    double[] chromagram = new double[NotesEnum.numberOfNotes];

    double[] audioSamplesBuffer;
    double[] prevAudioSamplesBuffer;
    double[] audioSpectrumBuffer;
    double[] prevAudioSpectrumBuffer;

    int mColor01;
    int mColor03;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setChordChart();
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
        audioSamplesBuffer = new double[Parameters.BUFFER_SIZE];
        audioSpectrumBuffer = new double[Parameters.BUFFER_SIZE];

        recordingButton = this.findViewById(R.id.recording_button);

        mColor01  = ResourcesCompat.getColor(getResources(), R.color.mColor01, null);
        mColor03  = ResourcesCompat.getColor(getResources(), R.color.mColor03, null);

        if (BuildConfig.FLAVOR.equals("dev")) {
            chordNoteText = this.findViewById(R.id.chord_note);
            chordTypeText = this.findViewById(R.id.chord_type);
            pitchText = this.findViewById(R.id.pitch_text);

            chordNoteText.setTextColor(mColor01);
            chordTypeText.setTextColor(mColor01);
            pitchText.setTextColor(mColor01);
        }

        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);

        mostProbableChordNoteText.setTextColor(mColor01);
        mostProbableChordNoteText.setText(NotesEnum.getString(NotesEnum.A));
        mostProbableChordTypeText.setTextColor(mColor01);
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

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processAudio() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final int[] chordDetectedThread = AudioStack.chordDetection(audioSamplesBuffer, audioSpectrumBuffer);
                final double[] chromagramThread = AudioStack.getChromagram(audioSamplesBuffer, audioSpectrumBuffer);

//                Log.l("MainActivityLog:: Spectrum diff: " + AudioStack.getDifference(audioSpectrumBuffer, prevAudioSpectrumBuffer));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chromagram = chromagramThread;
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
                final float pitchDetectedThread = AudioStack.getPitch(audioSamplesBuffer);
                final float pitchProbabilityThread = AudioStack.getPitchProbability();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pitchDetected = pitchDetectedThread;
                        pitchProbability = pitchProbabilityThread;
                        Log.l("PitchLog:: Pitch detected with probability " + pitchProbability);
                    }
                });
            }
        }).start();

        mostProbableChord = AudioStack.getMostProbableChord(chordsDetectedBuffer);

        if (canvas.getCanvas() != null) {
            canvas.updateCanvas(audioSamplesBuffer, audioSpectrumBuffer, AudioStack.getAverageLevel(audioSpectrumBuffer), pitchDetected, chromagram);
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

            TextView musicPlayingDetector;
            musicPlayingDetector = this.findViewById(R.id.music_playing_detector);
            musicPlayingDetector.setVisibility(View.VISIBLE);
            if (pitchProbability >= 0.80) {
                musicPlayingDetector.setTextColor(mColor01);
            } else {
                musicPlayingDetector.setTextColor(mColor03);
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

            switch(Parameters.getInstance().getTabSelected()) {
                case GUITAR_TAB:
                    GuitarChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f /*Percentage*/);
                    break;
                case UKULELE_TAB:
                    UkuleleChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f /*Percentage*/);
                    break;
                case CHROMAGRAM:
                    break;
            }
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
                short[] tempAudioSamples = new short[Parameters.BUFFER_SIZE];
                int numberOfShortRead;
                long totalShortsRead = 0;
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        Parameters.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT,
                        Parameters.BUFFER_SIZE);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.l("MainActivityLog:: Audio Record cannot be initialized!");
                    return;
                }
                record.startRecording();

                Log.l("MainActivityLog:: Start recording");

                mShouldContinue = true;
                final long startTime = System.currentTimeMillis();

                while (mShouldContinue) {
                    numberOfShortRead = record.read(tempAudioSamples, 0, tempAudioSamples.length);
                    totalShortsRead += numberOfShortRead;
                    prevAudioSpectrumBuffer = audioSpectrumBuffer;
                    audioSamplesBuffer = AudioStack.window(AudioStack.getSamplesToDouble(tempAudioSamples), Parameters.getInstance().getWindowingFunction());
                    audioSpectrumBuffer = AudioStack.bandPassFilter(AudioStack.fft(audioSamplesBuffer, true), 20, 8000);
//                    final double average = AudioStack.getAverageLevel(tempAudioSpectrum) * 25;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processAudio();
                        }
                    });
                    System.gc();
                    Log.l("MainActivityLog:: reading buffer of size " + Parameters.BUFFER_SIZE + ", Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
                }
                record.stop();
                record.release();

                Log.l("MainActivityLog:: Recording stopped. Num of samples read: " + totalShortsRead);
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
            case R.id.open_guitar_tab:
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.GUITAR_TAB);
                UkuleleChordChart.hideChordChart(this);
                setChordChart();
                return true;
            case R.id.open_ukulele_tab:
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.UKULELE_TAB);
                GuitarChordChart.hideChordChart(this);
                setChordChart();
                return true;
            case R.id.open_chromagram:
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.CHROMAGRAM);
                return true;
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

    private void setChordChart() {
        LinearLayout placeHolder;
        switch(Parameters.getInstance().getTabSelected()) {
            case GUITAR_TAB:
                placeHolder = this.findViewById(R.id.guitar_chord_chart_layout);
                getLayoutInflater().inflate(R.layout.guitar_chord_chart, placeHolder);
                break;
            case UKULELE_TAB:
                placeHolder = this.findViewById(R.id.ukulele_chord_chart_layout);
                getLayoutInflater().inflate(R.layout.ukulele_chord_chart, placeHolder);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    public void closeApp() {
        this.finish();
        System.exit(0);
    }
}