package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;
import com.upf.minichain.eversongapp.chordChart.GuitarChordChart;
import com.upf.minichain.eversongapp.chordChart.PianoChordChart;
import com.upf.minichain.eversongapp.chordChart.StaffChordChart;
import com.upf.minichain.eversongapp.chordChart.UkuleleChordChart;
import com.upf.minichain.eversongapp.enums.BroadcastExtra;
import com.upf.minichain.eversongapp.enums.BroadcastMessage;
import com.upf.minichain.eversongapp.enums.ChartTab;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.EversongFunctionalities;
import com.upf.minichain.eversongapp.enums.NotesEnum;

import java.util.ArrayList;

public class EversongActivity extends AppCompatActivity {
    EversongActivityBroadcastReceiver eversongBroadcastReceiver = new EversongActivityBroadcastReceiver();

    boolean keepProcessingFrame;
    Button recordingButton;
    TextView pitchText;
    TextView spectralFlatnessText;
    TextView musicPlayingDetectorText;

    // CHORD DETECTION
    TextView chordNoteText;
    TextView chordTypeText;
    TextView mostProbableChordNoteText;
    TextView mostProbableChordTypeText;

    // CHORD SCORE
    LinearLayout chordScoreLayout;
    NumberPicker chordScoreTonicNotePicker;
    NumberPicker chordScoreChordTypePicker;
    NotesEnum chordScoreTonicNotePicked;
    ChordTypeEnum chordScoreChordTypePicked;

    // TUNING
    TextView tuningPitchNote;
    LinearLayout tuningPitchNoteLayout;
    ImageView tuningPitchNoteTopArrow;
    ImageView tuningPitchNoteLeftArrow;
    ImageView tuningPitchNoteRightArrow;

    EversongCanvas canvas;

    boolean musicBeingPlayed;
    boolean polytonalMusicBeingPlayed;
    float pitchDetected;
    float pitchProbability;
    float[] pitchDetectedBuffer;
    NotesEnum pitchNote;
    float pitchNoteError;
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

    TextView versionNumberTextView;

    int mColor01;
    int mColor02;
    int mColor03;
    int mColor04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.l("EversongActivityLog:: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCaptureAudioPermission();

        LinearLayout placeHolder;

        placeHolder = this.findViewById(R.id.functionalities_menu_layout);
        getLayoutInflater().inflate(R.layout.functionalities_menu, placeHolder);

        placeHolder = this.findViewById(R.id.chart_menu_layout);
        getLayoutInflater().inflate(R.layout.chart_menu, placeHolder);

        inflateChordChartLayouts();
        inflateChordScoreLayout();
        inflateTuningPitchNoteLayout();
    }

    @Override
    protected void onRestart() {
        Log.l("EversongActivityLog:: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.l("EversongActivityLog:: onResume");
        initMainActivity();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.l("EversongActivityLog:: onPause");
        super.onPause();
        stopRecording();
        try {
            unregisterReceiver(eversongBroadcastReceiver);
        } catch(IllegalArgumentException e) {

        }
    }

    @Override
    protected void onStop() {
        Log.l("EversongActivityLog:: onStop");
        super.onStop();
        stopRecording();
        try {
            unregisterReceiver(eversongBroadcastReceiver);
        } catch(IllegalArgumentException e) {

        }
    }

    public void initMainActivity() {
        Log.l("EversongActivityLog:: initMainActivity");
        Parameters.getInstance().loadParameters(getApplicationContext());

        Intent serviceIntent = new Intent(getApplicationContext(), EversongService.class);
        getApplicationContext().startService(serviceIntent);
        registerEversongActivityBroadcastReceiver();

        musicBeingPlayed = false;
        polytonalMusicBeingPlayed = false;
        pitchDetected = -1;
        pitchNote = NotesEnum.NO_NOTE;
        pitchNoteError = 0;
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

        mColor01 = ResourcesCompat.getColor(getResources(), R.color.mColor01, null);
        mColor02 = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        mColor03 = ResourcesCompat.getColor(getResources(), R.color.mColor03, null);
        mColor04 = ResourcesCompat.getColor(getResources(), R.color.mColor04, null);

        // CHORD DETECTION
        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);
        mostProbableChordNoteText.setTextColor(mColor01);
        mostProbableChordNoteText.setText(NotesEnum.A.toString());
        mostProbableChordTypeText.setTextColor(mColor01);
        mostProbableChordTypeText.setText(ChordTypeEnum.Major.toString());

        // CHORD SCORE
        chordScoreLayout = this.findViewById(R.id.chord_score_layout);

        // TUNING
        tuningPitchNote = this.findViewById(R.id.tuning_pitch_note);
        tuningPitchNote.setTextColor(mColor01);
        tuningPitchNote.setText(NotesEnum.NO_NOTE.toString());
        tuningPitchNoteLayout = this.findViewById(R.id.tuning_pitch_note_layout);
        tuningPitchNoteTopArrow = this.findViewById(R.id.tuning_pitch_note_top_arrow);
        tuningPitchNoteLeftArrow = this.findViewById(R.id.tuning_pitch_note_left_arrow);
        tuningPitchNoteRightArrow = this.findViewById(R.id.tuning_pitch_note_right_arrow);

        algorithmPerformanceText = this.findViewById(R.id.algorithm_performance);
        algorithmPerformanceText.setTextColor(mColor01);

        versionNumberTextView = this.findViewById(R.id.version_number_text);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "v" + pInfo.versionName;
            versionNumberTextView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setDebugModeViews();
        setFunctionality();
        setChordChart();
        setFunctionalitiesMenu();
        setChartMenu();
        setChordScoreViews();
        setCanvas();

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
                    startRecording();
                } else if (recordingButton.getText().equals(getString(R.string.stop_record_button))) {
                    stopRecording();
                }
            }
        });
    }

    private void setChordScoreViews() {
        chordScoreTonicNotePicker = this.findViewById(R.id.tonic_note_picker);
        chordScoreTonicNotePicker.setTextColor(mColor01);
        chordScoreTonicNotePicker.setSelectedTextColor(mColor01);
        String[] tonicNotePickerData = new String[NotesEnum.numberOfNotes];
        for (int i = 0; i < NotesEnum.numberOfNotes; i++) {
            tonicNotePickerData[i] = NotesEnum.values()[i].toString();
        }
        chordScoreTonicNotePicker.setMinValue(1);   //It must start at 1
        chordScoreTonicNotePicker.setMaxValue(NotesEnum.numberOfNotes);
        chordScoreTonicNotePicker.setDisplayedValues(tonicNotePickerData);
        chordScoreTonicNotePicked = NotesEnum.values()[chordScoreTonicNotePicker.getValue() - 1];
        chordScoreTonicNotePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                chordScoreTonicNotePicked = NotesEnum.values()[newVal - 1];
            }
        });

        chordScoreChordTypePicker = this.findViewById(R.id.chord_type_picker);
        chordScoreChordTypePicker.setTextColor(mColor01);
        chordScoreChordTypePicker.setSelectedTextColor(mColor01);
        String[] chordTypePickerData = new String[ChordTypeEnum.numberOfChordTypes];
        for (int i = 0; i < ChordTypeEnum.numberOfChordTypes; i++) {
            chordTypePickerData[i] = ChordTypeEnum.values()[i].toString();
        }
        chordScoreChordTypePicker.setMinValue(1);   //It must start at 1
        chordScoreChordTypePicker.setMaxValue(ChordTypeEnum.numberOfChordTypes);
        chordScoreChordTypePicker.setDisplayedValues(chordTypePickerData);
        chordScoreChordTypePicked = ChordTypeEnum.values()[chordScoreChordTypePicker.getValue() - 1];
        chordScoreChordTypePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                chordScoreChordTypePicked = ChordTypeEnum.values()[newVal - 1];
            }
        });
    }

    private void setCanvas() {
        int screenHeight = Utils.getScreenHeightInPixels(this);
        ConstraintLayout.LayoutParams params;
        ImageView canvasView = this.findViewById(R.id.canvas_view);
        params = (ConstraintLayout.LayoutParams) canvasView.getLayoutParams();
        params.height = screenHeight - (int)Utils.convertSpToPixels(120, getApplicationContext());
//        params.height = screenHeight - this.findViewById(R.id.functionalities_menu_layout).getLayoutParams().height - this.findViewById(R.id.chart_menu_layout).getLayoutParams().height;
        canvasView.setLayoutParams(params);
        canvas = new EversongCanvas(getResources(), canvasView);
    }

    private void startRecording() {
        sendBroadcastToService(BroadcastMessage.START_RECORDING_AUDIO);
        recordingButton.setText(R.string.stop_record_button);
        detectedChordsFile.startTime = System.currentTimeMillis();
        arrayOfChordsDetected.clear();
    }

    private void stopRecording() {
        recordingButton.setText(R.string.start_record_button);
        sendBroadcastToService(BroadcastMessage.STOP_RECORDING_AUDIO);
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
            updateDebugModeViews();
        }

        switch (Parameters.getInstance().getFunctionalitySelected()) {
            case CHORD_DETECTION:
                updateChordDetectionViews();
                break;
            case CHORD_SCORE:
                updateChordScoreViews();
                break;
            case TUNING:
                updateTuningViews();
                break;
        }
    }

    private void updateChordDetectionViews() {
        float alpha = ((float)mostProbableChord[2] / 100f) > 0.5 ? ((float)mostProbableChord[2] / 100f) : 0.5f;
        if (mostProbableChord[0] != -1 && mostProbableChord[1] != -1) {
            if (mostProbableChordNoteText.getVisibility() == View.VISIBLE) {
                mostProbableChordNoteText.setText(NotesEnum.fromInteger(mostProbableChord[0]).toString());
                mostProbableChordNoteText.setAlpha(alpha);
            }
            if (mostProbableChordTypeText.getVisibility() == View.VISIBLE) {
                mostProbableChordTypeText.setText(ChordTypeEnum.fromInteger(mostProbableChord[1]).toString());
                mostProbableChordTypeText.setAlpha(alpha);
            }
        }
        switch(Parameters.getInstance().getChartTabSelected()) {
            case GUITAR_TAB:
                GuitarChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), alpha);
                break;
            case UKULELE_TAB:
                UkuleleChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), alpha);
                break;
            case PIANO_TAB:
                PianoChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), alpha);
                break;
            case STAFF_TAB:
                StaffChordChart.setChordChart(this, NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]), alpha);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    private void updateChordScoreViews() {
        if (chordScoreTonicNotePicked == NotesEnum.fromInteger(mostProbableChord[0])
                && chordScoreChordTypePicked == ChordTypeEnum.fromInteger(mostProbableChord[1])) {
            this.findViewById(R.id.chord_score_green_tick).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.chord_score_green_tick).setVisibility(View.GONE);
        }

        switch(Parameters.getInstance().getChartTabSelected()) {
            case GUITAR_TAB:
                GuitarChordChart.setChordChart(this, chordScoreTonicNotePicked, chordScoreChordTypePicked, 1f);
                break;
            case UKULELE_TAB:
                UkuleleChordChart.setChordChart(this, chordScoreTonicNotePicked, chordScoreChordTypePicked, 1f);
                break;
            case PIANO_TAB:
                PianoChordChart.setChordChart(this, chordScoreTonicNotePicked, chordScoreChordTypePicked, 1f);
                break;
            case STAFF_TAB:
                StaffChordChart.setChordChart(this, chordScoreTonicNotePicked, chordScoreChordTypePicked, 1f);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    private void updateTuningViews() {
        if (pitchDetected != -1) {
            float[] tempPitch = AudioStack.getNoteByFrequencyAndError((double)pitchDetected, Parameters.BANDPASS_FILTER_LOW_FREQ, Parameters.BANDPASS_FILTER_HIGH_FREQ);
            pitchNote = NotesEnum.fromInteger((int)tempPitch[0]);
            pitchNoteError = tempPitch[1];
            tuningPitchNoteTopArrow.setAlpha(1f - Math.abs(pitchNoteError));
            if (pitchNoteError < 0) {
                tuningPitchNoteRightArrow.setAlpha(Math.abs(pitchNoteError));
                tuningPitchNoteLeftArrow.setAlpha(0f);
            } else {
                tuningPitchNoteLeftArrow.setAlpha(pitchNoteError);
                tuningPitchNoteRightArrow.setAlpha(0f);
            }
        } else {
            pitchNote = NotesEnum.NO_NOTE;
            tuningPitchNoteTopArrow.setAlpha(0f);
            tuningPitchNoteLeftArrow.setAlpha(0f);
            tuningPitchNoteRightArrow.setAlpha(0f);
            pitchNoteError = 0;
        }
        if (tuningPitchNote.getVisibility() == View.VISIBLE) {
            tuningPitchNote.setText(pitchNote.toString());
        }
        switch(Parameters.getInstance().getChartTabSelected()) {
            case GUITAR_TAB:
                GuitarChordChart.setTuningChordChart(this, pitchNote, pitchDetected);
                break;
            case UKULELE_TAB:
                UkuleleChordChart.setTuningChordChart(this, pitchNote, pitchDetected);
                break;
            case PIANO_TAB:
                PianoChordChart.setTuningChordChart(this, pitchNote, pitchDetected);
                break;
            case STAFF_TAB:
                StaffChordChart.setTuningChordChart(this, pitchNote);
                break;
            case CHROMAGRAM:
                break;
        }
    }

    private void updateDebugModeViews() {
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
                sendBroadcastToService(BroadcastMessage.PAUSE_ACTIVITY);
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
        Log.l("EversongActivityLog:: sending broadcast " + broadcastMessage.toString());
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

                    } else if (broadcast.equals(BroadcastMessage.ALGORITHM_PERFORMANCE.toString())) {
                        algorithmPerformanceText.setVisibility(View.VISIBLE);
                        algorithmPerformanceText.setText("Performance: " + (int)(extras.getDouble(BroadcastExtra.ALGORITHM_PERFORMANCE.toString()) * 100) + "%");
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

    private void setFunctionality() {
        switch(Parameters.getInstance().getFunctionalitySelected()) {
            case CHORD_DETECTION:
            default:
                mostProbableChordNoteText.setVisibility(View.VISIBLE);
                mostProbableChordTypeText.setVisibility(View.VISIBLE);
                tuningPitchNoteLayout.setVisibility(View.GONE);
                tuningPitchNote.setVisibility(View.GONE);
                chordScoreLayout.setVisibility(View.GONE);
                break;
            case CHORD_SCORE:
                mostProbableChordNoteText.setVisibility(View.GONE);
                mostProbableChordTypeText.setVisibility(View.GONE);
                tuningPitchNoteLayout.setVisibility(View.GONE);
                tuningPitchNote.setVisibility(View.GONE);
                chordScoreLayout.setVisibility(View.VISIBLE);
                break;
            case TUNING:
                mostProbableChordNoteText.setVisibility(View.GONE);
                mostProbableChordTypeText.setVisibility(View.GONE);
                tuningPitchNoteLayout.setVisibility(View.VISIBLE);
                tuningPitchNote.setVisibility(View.VISIBLE);
                chordScoreLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void inflateChordChartLayouts() {
        LinearLayout placeHolder;

        placeHolder = this.findViewById(R.id.guitar_chord_chart_layout);
        getLayoutInflater().inflate(R.layout.guitar_chord_chart, placeHolder);

        placeHolder = this.findViewById(R.id.ukulele_chord_chart_layout);
        getLayoutInflater().inflate(R.layout.ukulele_chord_chart, placeHolder);

        placeHolder = this.findViewById(R.id.piano_chord_chart_layout);
        getLayoutInflater().inflate(R.layout.piano_chord_chart, placeHolder);

        placeHolder = this.findViewById(R.id.staff_chord_chart_layout);
        getLayoutInflater().inflate(R.layout.staff_chord_chart, placeHolder);
    }

    private void inflateChordScoreLayout() {
        LinearLayout placeHolder;

        placeHolder = this.findViewById(R.id.chord_score_layout);
        getLayoutInflater().inflate(R.layout.chord_score, placeHolder);
    }

    private void inflateTuningPitchNoteLayout() {
        LinearLayout placeHolder;

        placeHolder = this.findViewById(R.id.tuning_pitch_note_layout);
        getLayoutInflater().inflate(R.layout.tuning_pitch_note, placeHolder);
    }

    private void setChordChart() {
        switch(Parameters.getInstance().getChartTabSelected()) {
            case GUITAR_TAB:
            default:
                UkuleleChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                PianoChordChart.hideChordChart(this);
                break;
            case UKULELE_TAB:
                GuitarChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                PianoChordChart.hideChordChart(this);
                break;
            case PIANO_TAB:
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                break;
            case STAFF_TAB:
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                PianoChordChart.hideChordChart(this);
                break;
            case CHROMAGRAM:
                GuitarChordChart.hideChordChart(this);
                UkuleleChordChart.hideChordChart(this);
                StaffChordChart.hideChordChart(this);
                PianoChordChart.hideChordChart(this);
                break;
        }
    }

    private void setFunctionalitiesMenu() {
        int screenWidth = Utils.getScreenWidthInPixels(this);
        ConstraintLayout.LayoutParams params;

        Button button = this.findViewById(R.id.chord_detection_functionality_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ EversongFunctionalities.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getFunctionalitySelected() != EversongFunctionalities.CHORD_DETECTION) {
                    Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.CHORD_DETECTION);
                    setFunctionality();
                    updateFunctionalitiesMenu();
                }
            }
        });

        button = this.findViewById(R.id.chord_score_functionality_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ EversongFunctionalities.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getFunctionalitySelected() != EversongFunctionalities.CHORD_SCORE) {
                    Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.CHORD_SCORE);
                    setFunctionality();
                    updateFunctionalitiesMenu();
                }
            }
        });

        button = this.findViewById(R.id.tuning_functionality_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ EversongFunctionalities.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getFunctionalitySelected() != EversongFunctionalities.TUNING) {
                    Parameters.getInstance().setFunctionalitySelected(EversongFunctionalities.TUNING);
                    setFunctionality();
                    updateFunctionalitiesMenu();
                }
            }
        });

        updateFunctionalitiesMenu();
    }

    private void updateFunctionalitiesMenu() {
        Button button;

        button = this.findViewById(R.id.chord_detection_functionality_button);
        if (Parameters.getInstance().getFunctionalitySelected() == EversongFunctionalities.CHORD_DETECTION) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.chord_score_functionality_button);
        if (Parameters.getInstance().getFunctionalitySelected() == EversongFunctionalities.CHORD_SCORE) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.tuning_functionality_button);
        if (Parameters.getInstance().getFunctionalitySelected() == EversongFunctionalities.TUNING) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }
    }

    private void setChartMenu() {
        int screenWidth = Utils.getScreenWidthInPixels(this);
        ConstraintLayout.LayoutParams params;

        Button button = this.findViewById(R.id.guitar_chart_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ ChartTab.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getChartTabSelected() != ChartTab.GUITAR_TAB) {
                    Parameters.getInstance().setChartTabSelected(ChartTab.GUITAR_TAB);
                    setChordChart();
                    updateChartMenu();
                }
            }
        });

        button = this.findViewById(R.id.ukulele_chart_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ ChartTab.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getChartTabSelected() != ChartTab.UKULELE_TAB) {
                    Parameters.getInstance().setChartTabSelected(ChartTab.UKULELE_TAB);
                    setChordChart();
                    updateChartMenu();
                }
            }
        });

        button = this.findViewById(R.id.piano_chart_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ ChartTab.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getChartTabSelected() != ChartTab.PIANO_TAB) {
                    Parameters.getInstance().setChartTabSelected(ChartTab.PIANO_TAB);
                    setChordChart();
                    updateChartMenu();
                }
            }
        });

        button = this.findViewById(R.id.staff_chart_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ ChartTab.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getChartTabSelected() != ChartTab.STAFF_TAB) {
                    Parameters.getInstance().setChartTabSelected(ChartTab.STAFF_TAB);
                    setChordChart();
                    updateChartMenu();
                }
            }
        });

        button = this.findViewById(R.id.chromagram_chart_button);
        params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = screenWidth/ ChartTab.values().length;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Parameters.getInstance().getChartTabSelected() != ChartTab.CHROMAGRAM) {
                    Parameters.getInstance().setChartTabSelected(ChartTab.CHROMAGRAM);
                    setChordChart();
                    updateChartMenu();
                }
            }
        });

        updateChartMenu();
    }

    private void updateChartMenu() {
        Button button;

        button = this.findViewById(R.id.guitar_chart_button);
        if (Parameters.getInstance().getChartTabSelected() == ChartTab.GUITAR_TAB) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.ukulele_chart_button);
        if (Parameters.getInstance().getChartTabSelected() == ChartTab.UKULELE_TAB) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.piano_chart_button);
        if (Parameters.getInstance().getChartTabSelected() == ChartTab.PIANO_TAB) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.staff_chart_button);
        if (Parameters.getInstance().getChartTabSelected() == ChartTab.STAFF_TAB) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
        }

        button = this.findViewById(R.id.chromagram_chart_button);
        if (Parameters.getInstance().getChartTabSelected() == ChartTab.CHROMAGRAM) {
            button.setBackgroundResource(R.drawable.chart_menu_button_selected);
            button.setTextColor(mColor01);
        } else {
            button.setBackgroundResource(R.drawable.chart_menu_button_not_selected);
            button.setTextColor(mColor02);
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