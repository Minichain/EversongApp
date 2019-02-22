package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import com.upf.minichain.eversongapp.chordChart.GuitarChordChart;
import com.upf.minichain.eversongapp.chordChart.StaffChordChart;
import com.upf.minichain.eversongapp.chordChart.UkuleleChordChart;
import com.upf.minichain.eversongapp.enums.BroadcastExtra;
import com.upf.minichain.eversongapp.enums.BroadcastMessage;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

import java.util.ArrayList;

public class EversongActivity extends AppCompatActivity {
    EversongActivityBroadcastReceiver eversongBroadcastReceiver;

    boolean keepRecordingAudio;        // Indicates if recording / playback should stop
    boolean keepProcessingFrame;
    Button recordingButton;
    TextView pitchText;
    TextView spectralFlatnessText;
    TextView chordNoteText;
    TextView mostProbableChordNoteText;
    TextView chordTypeText;
    TextView mostProbableChordTypeText;
    TextView musicPlayingDetectorText;
    EversongCanvas canvas;

    boolean musicBeingPlayed;
    boolean polytonalMusicBeingPlayed;
    float pitchDetected;
    float pitchProbability;
    float[] pitchDetectedBuffer;
    NotesEnum pitchNote;
    double spectralFlatnessValue;
    int[] chordDetected = new int[2];
    int[][] mostProbableChordBuffer;

    ArrayList<String> arrayOfChordsDetected;
    DetectedChordFile detectedChordsFile;

    int[] mostProbableChord = new int[3];
    double[] chromagram = new double[NotesEnum.numberOfNotes];

    double[] audioSamplesBuffer;
    double[] audioSpectrumBuffer;

    TextView algorithmPerformanceText;

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
        initMainActivity();
        super.onResume();
    }

    @Override
    protected void onPause() {
        keepRecordingAudio = false;
        unregisterReceiver(eversongBroadcastReceiver);
        super.onPause();
    }

    public void initMainActivity() {
        Log.l("EversongActivityLog:: initMainActivity");
        Parameters.getInstance().loadParameters(getApplicationContext());
        AudioStack.initAudioStack();

        Intent serviceIntent = new Intent(getApplicationContext(), EversongService.class);
        getApplicationContext().startService(serviceIntent);

        eversongBroadcastReceiver = new EversongActivityBroadcastReceiver();
        registerEversongActivityBroadcastReceiver();

        musicBeingPlayed = false;
        polytonalMusicBeingPlayed = false;
        pitchDetected = -1;
        pitchNote = NotesEnum.NO_NOTE;
        pitchDetectedBuffer = new float[Parameters.getInstance().getPitchBufferSize()];
        chordDetected[0] = -1;
        chordDetected[1] = -1;
        mostProbableChord[0] = NotesEnum.A.getValue();
        mostProbableChord[1] = ChordTypeEnum.Major.getValue();
        mostProbableChord[2] = 100;
        mostProbableChordBuffer = new int[Parameters.getInstance().getChordBufferSize()][2];
        arrayOfChordsDetected =  new ArrayList<>();
        detectedChordsFile = new DetectedChordFile(getApplicationContext());
        audioSamplesBuffer = new double[Parameters.BUFFER_SIZE];
        audioSpectrumBuffer = new double[Parameters.BUFFER_SIZE];

        recordingButton = this.findViewById(R.id.recording_button);
        recordingButton.setText(R.string.start_record_button);

        mColor01  = ResourcesCompat.getColor(getResources(), R.color.mColor01, null);
        mColor03  = ResourcesCompat.getColor(getResources(), R.color.mColor03, null);

        setDebugModeViews();

        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);

        mostProbableChordNoteText.setTextColor(mColor01);
        mostProbableChordNoteText.setText(NotesEnum.A.toString());
        mostProbableChordTypeText.setTextColor(mColor01);
        mostProbableChordTypeText.setText(ChordTypeEnum.Major.toString());

        algorithmPerformanceText = this.findViewById(R.id.algorithm_performance);
        algorithmPerformanceText.setTextColor(mColor01);

        canvas =  new EversongCanvas(getResources(), this.findViewById(R.id.canvas_view));

        keepProcessingFrame = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(keepProcessingFrame) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    try {
                        Thread.sleep(1000 / Parameters.FRAMES_PER_SECOND);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processFrame();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.l("EversongActivityLog:: recordingButton pressed!");
                if (recordingButton.getText().equals(getString(R.string.start_record_button))) {
                    sendBroadcastToService(BroadcastMessage.START_RECORDING_AUDIO);
                    recordingButton.setText(R.string.stop_record_button);
                    detectedChordsFile.startTime = System.currentTimeMillis();
                    arrayOfChordsDetected.clear();
                } else if (recordingButton.getText().equals(getString(R.string.stop_record_button))) {
                    recordingButton.setText(R.string.start_record_button);
                    sendBroadcastToService(BroadcastMessage.STOP_RECORDING_AUDIO);
                    if (Parameters.getInstance().isDebugMode()) {
                        algorithmPerformanceText.setVisibility(View.VISIBLE);
                        algorithmPerformanceText.setText("Performance: " + (int)(TestAlgorithm.computeAlgorithmPerformance(arrayOfChordsDetected) * 100) + "%");
//                        Log.l("AlgorithmPerformanceLog:: Performance: " + (int)(TestAlgorithm.computeAlgorithmPerformance(arrayOfChordsDetected) * 100) + "%");
                    }
                }
            }
        });
    }

    private void setDebugModeViews() {
        chordNoteText = this.findViewById(R.id.chord_note);
        chordTypeText = this.findViewById(R.id.chord_type);
        pitchText = this.findViewById(R.id.pitch_text);
        spectralFlatnessText = this.findViewById(R.id.spectral_flatness_text);
        musicPlayingDetectorText = this.findViewById(R.id.music_playing_detector);

        if (Parameters.getInstance().isDebugMode()) {
            chordNoteText.setVisibility(View.VISIBLE);
            chordTypeText.setVisibility(View.VISIBLE);
            pitchText.setVisibility(View.VISIBLE);
            spectralFlatnessText.setVisibility(View.VISIBLE);
            musicPlayingDetectorText.setVisibility(View.VISIBLE);

            chordNoteText.setTextColor(mColor01);
            chordTypeText.setTextColor(mColor01);
            pitchText.setTextColor(mColor01);
            spectralFlatnessText.setTextColor(mColor01);
            musicPlayingDetectorText.setTextColor(mColor01);
        } else {
            chordNoteText.setVisibility(View.GONE);
            chordTypeText.setVisibility(View.GONE);
            pitchText.setVisibility(View.GONE);
            spectralFlatnessText.setVisibility(View.GONE);
            musicPlayingDetectorText.setVisibility(View.GONE);
        }
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processFrame() {
        if (canvas.getCanvas() != null) {
            canvas.updateCanvas(audioSamplesBuffer, audioSpectrumBuffer, AudioStack.getAverageLevel(audioSpectrumBuffer), pitchDetected, chromagram, chordDetected);
        }

        if (Parameters.getInstance().isDebugMode()) {
            if (pitchDetected != -1) {
                pitchNote = AudioStack.getNoteByFrequency((double)pitchDetected);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Pitch: ").append((int)pitchDetected).append(" Hz").append(" (")
                        .append(pitchNote.toString()).append("), ")
                        .append((int)(pitchProbability * 100)).append("%");
                pitchText.setText(stringBuilder.toString());
            } else {
                pitchNote = NotesEnum.NO_NOTE;
                pitchText.setText(String.valueOf("Pitch: " + pitchNote.toString() + " Hz"));
            }

            if (chordDetected[0] != -1) {
                chordNoteText.setText(NotesEnum.fromInteger(chordDetected[0]).toString());
            }

            if (chordDetected[1] != -1) {
                chordTypeText.setText(ChordTypeEnum.fromInteger(chordDetected[1]).toString());
            }

            spectralFlatnessText.setText("Flatness: 0." + ((int)(spectralFlatnessValue * 1000000)));
            if (musicBeingPlayed) {
                musicPlayingDetectorText.setVisibility(View.VISIBLE);
                if (polytonalMusicBeingPlayed) {
                    musicPlayingDetectorText.setText("POLYTONAL MUSIC PLAYING!");
                } else {
                    musicPlayingDetectorText.setText("MONOTONAL MUSIC PLAYING!");
                }
            } else {
                musicPlayingDetectorText.setVisibility(View.GONE);
            }
        }

        if (mostProbableChord[0] != -1 && mostProbableChord[1] != -1) {
            mostProbableChordNoteText.setText(NotesEnum.fromInteger(mostProbableChord[0]).toString());
            mostProbableChordNoteText.setAlpha((float)mostProbableChord[2] / 100f);
            mostProbableChordTypeText.setText(ChordTypeEnum.fromInteger(mostProbableChord[1]).toString());
            mostProbableChordTypeText.setAlpha((float)mostProbableChord[2] / 100f);
        }

        updateDetectedChordsList();

        switch(Parameters.getInstance().getTabSelected()) {
            case GUITAR_TAB:
                GuitarChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f /*Percentage*/);
                break;
            case UKULELE_TAB:
                UkuleleChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f /*Percentage*/);
                break;
            case PIANO_TAB:
                //TODO
                break;
            case STAFF_TAB:
                StaffChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), (float)mostProbableChord[2] / 100f /*Percentage*/);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    private void updateDetectedChordsList() {
        String chord = NotesEnum.fromInteger(mostProbableChord[0]).toString() + " " + ChordTypeEnum.fromInteger(mostProbableChord[1]).toString();
        if (!keepRecordingAudio) {
            return;
        }
        if (arrayOfChordsDetected.isEmpty() || !arrayOfChordsDetected.get(arrayOfChordsDetected.size() - 1).contains(chord)) {
            long timeInMillis = (System.currentTimeMillis() - detectedChordsFile.startTime);
            String newElement = timeInMillis + " ms"  + ": " + chord;
            arrayOfChordsDetected.add(newElement);
            detectedChordsFile.writeInFile(newElement);
        }
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
                StaffChordChart.hideChordChart(this);
                setChordChart();
                return true;
            case R.id.open_ukulele_tab:
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.UKULELE_TAB);
                GuitarChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                setChordChart();
                return true;
            case R.id.open_staff_tab:
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.STAFF_TAB);
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                setChordChart();
                return true;
            case R.id.open_chromagram:
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                Parameters.getInstance().setTabSelected(Parameters.TabSelected.CHROMAGRAM);
                return true;
            case R.id.open_settings_menu_option:
                keepProcessingFrame = false;
                this.onPause();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.exit_app_option:
                keepProcessingFrame = false;
                detectedChordsFile.closeFile();
                closeApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendBroadcastToService(BroadcastMessage broadcastMessage) {
        Log.l("EversongServiceLog:: sending broadcast " + broadcastMessage.toString());
        try {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(broadcastMessage.toString());

            sendBroadcast(broadCastIntent);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class EversongActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.l("EversongActivityLog:: Broadcast received " + intent.getAction());
            try {
                String broadcast = intent.getAction();
                Bundle extras = intent.getExtras();
                if (broadcast != null) {
                    if (broadcast.equals(BroadcastMessage.REFRESH_FRAME.toString())) {
                        Log.l("EversongActivityLog:: refreshing frame!");
                    } else if (broadcast.equals(BroadcastMessage.START_RECORDING_AUDIO.toString())) {

                    } else if (broadcast.equals(BroadcastMessage.STOP_RECORDING_AUDIO.toString())) {

                    } else if (broadcast.equals(BroadcastMessage.PITCH_DETECTION.toString())) {
                        pitchDetected = extras.getFloat(BroadcastExtra.PITCH_DETECTED.toString());
                        pitchProbability = extras.getFloat(BroadcastExtra.PITCH_PROBABILITY.toString());
                    } else if (broadcast.equals(BroadcastMessage.AUDIO_CAPTURED.toString())) {
                        audioSamplesBuffer = extras.getDoubleArray(BroadcastExtra.AUDIO_SAMPLES_BUFFER.toString());
                        audioSpectrumBuffer = extras.getDoubleArray(BroadcastExtra.AUDIO_SPECTRUM_BUFFER.toString());
                    } else if (broadcast.equals(BroadcastMessage.MUSIC_DETECTION.toString())) {
                        musicBeingPlayed = extras.getBoolean(BroadcastExtra.MUSIC_BEING_PLAYED.toString());
                        polytonalMusicBeingPlayed = extras.getBoolean(BroadcastExtra.POLYTONAL_MUSIC_BEING_PLAYED.toString());
                        spectralFlatnessValue = extras.getDouble(BroadcastExtra.SPECTRAL_FLATNESS.toString());
                    } else if (broadcast.equals(BroadcastMessage.CHORD_DETECTION_PROCESSED.toString())) {
                        chromagram = extras.getDoubleArray(BroadcastExtra.CHROMAGRAM.toString());
                        chordDetected = extras.getIntArray(BroadcastExtra.CHORD_DETECTED.toString());
                        mostProbableChord = extras.getIntArray(BroadcastExtra.MOST_PROBABLE_CHORD.toString());
                        mostProbableChordBuffer = (int[][])extras.getSerializable(BroadcastExtra.MOST_PROBABLE_CHORD_BUFFER.toString());
                    } else {

                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void registerEversongActivityBroadcastReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            for (int i = 0; i < BroadcastMessage.values().length; i++) {
                intentFilter.addAction(BroadcastMessage.values()[i].toString());
            }
            registerReceiver(eversongBroadcastReceiver, intentFilter);
        }
        catch (Exception ex) {
            ex.printStackTrace();
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
            case STAFF_TAB:
                placeHolder = this.findViewById(R.id.staff_chord_chart_layout);
                getLayoutInflater().inflate(R.layout.staff_chord_chart, placeHolder);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    public void closeApp() {
        Intent serviceIntent = new Intent(getApplicationContext(), EversongService.class);
        getApplicationContext().stopService(serviceIntent);
        this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(-1);
    }
}