package com.upf.minichain.eversongapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

import java.util.ArrayList;

public class EversongActivity extends AppCompatActivity {
    EversongActivityBroadcastReceiver eversongBroadcastReceiver;

    boolean keepRecordingAudio;
    boolean keepProcessingFrame;

    ImageButton recordingButton;
    TextView pitchText;
    TextView spectralFlatnessText;
    ImageView musicPlayingDetectorImageView;

    // CHORD DETECTION
    LinearLayout chordDetectedLayout;
    TextView chordNoteText;
    TextView chordTypeText;
    TextView chordProbabilityText;
    TextView mostProbableChordNoteText;
    TextView mostProbableChordTypeText;
    TextView previousMostProbableChordNoteText;
    TextView previousMostProbableChordTypeText;

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
    float chordDetectedProbability;
    int[][] mostProbableChordBuffer;

    ArrayList<String> arrayOfChordsDetected;
    DetectedChordFile detectedChordsFile;

    int[] mostProbableChord = new int[3];
    double[] chromagram = new double[NotesEnum.numberOfNotes];

    double[] audioSamplesBuffer;
    double[] audioSpectrumBuffer;

    TextView algorithmPerformanceText;

    TextView versionNumberTextView;

    ChartFloatingMenu chartFloatingMenu;
    FunctionalitiesFloatingMenu functionalitiesFloatingMenu;

    int colorWhite;
    int colorPrimary;
    int colorPrimaryDark;
    int colorBackgroundDark;
    int colorGreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.l("EversongActivityLog:: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCaptureAudioPermission();

        inflateChordDetectedLayout();
        inflateChordChartLayouts();
        inflateChordScoreLayout();
        inflateTuningPitchNoteLayout();

        initMainActivity();
    }

    @Override
    protected void onRestart() {
        Log.l("EversongActivityLog:: onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.l("EversongActivityLog:: onStart");
        super.onStart();
        registerEversongActivityBroadcastReceiver();
        startProcessingFrames();
    }

    @Override
    protected void onResume() {
        Log.l("EversongActivityLog:: onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.l("EversongActivityLog:: onPause");
        super.onPause();
        stopRecording();
        try {
            unregisterReceiver(eversongBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("EversongActivityLog:: error un-registering receiver " + e);
        }
    }

    @Override
    protected void onStop() {
        Log.l("EversongActivityLog:: onStop");
        super.onStop();
        stopRecording();
        try {
            unregisterReceiver(eversongBroadcastReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    public void initMainActivity() {
        Log.l("EversongActivityLog:: initMainActivity");
        Parameters.loadParameters(getApplicationContext());

        Intent serviceIntent = new Intent(getApplicationContext(), EversongService.class);

        //Start service:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                getApplicationContext().startForegroundService(serviceIntent);
                Log.l("EversongActivityLog:: startForegroundService");
            } catch (Exception e) {
                getApplicationContext().startService(serviceIntent);
                Log.l("EversongActivityLog:: startService");
            }
        } else {
            Log.l("EversongActivityLog:: startService");
            getApplicationContext().startService(serviceIntent);
        }

        musicBeingPlayed = false;
        polytonalMusicBeingPlayed = false;
        pitchDetected = -1;
        pitchNote = NotesEnum.NO_NOTE;
        pitchNoteError = 0;
        pitchDetectedBuffer = new float[Parameters.getPitchBufferSize()];
        chordDetected[0] = -1;
        chordDetected[1] = -1;
        chordDetectedProbability = -1;
        mostProbableChord[0] = NotesEnum.A.getValue();
        mostProbableChord[1] = ChordTypeEnum.Major.getValue();
        mostProbableChord[2] = 100;
        mostProbableChordBuffer = new int[Parameters.getChordBufferSize()][2];
        arrayOfChordsDetected =  new ArrayList<>();
        detectedChordsFile = new DetectedChordFile(getApplicationContext());
        audioSamplesBuffer = new double[Parameters.BUFFER_SIZE];
        audioSpectrumBuffer = new double[Parameters.BUFFER_SIZE];

        recordingButton = this.findViewById(R.id.recording_button);
        if (keepRecordingAudio) {
            recordingButton.setImageResource(R.drawable.baseline_mic_white_24);
        } else {
            recordingButton.setImageResource(R.drawable.baseline_mic_off_white_24);
        }

        colorWhite = ResourcesCompat.getColor(getResources(), R.color.colorWhite, null);
        colorPrimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        colorPrimaryDark = ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null);
        colorBackgroundDark = ResourcesCompat.getColor(getResources(), R.color.colorBackgroundDark, null);
        colorGreen = ResourcesCompat.getColor(getResources(), R.color.colorGreen, null);

        // CHORD DETECTION
        chordDetectedLayout = this.findViewById(R.id.chord_detected_layout);

        mostProbableChordNoteText = this.findViewById(R.id.most_probable_chord_note);
        mostProbableChordTypeText = this.findViewById(R.id.most_probable_chord_type);
        mostProbableChordNoteText.setTextColor(colorWhite);
        mostProbableChordNoteText.setText(NotesEnum.A.toString());
        mostProbableChordTypeText.setTextColor(colorWhite);
        mostProbableChordTypeText.setText(ChordTypeEnum.Major.toString());

        previousMostProbableChordNoteText = this.findViewById(R.id.previous_most_probable_chord_note);
        previousMostProbableChordTypeText = this.findViewById(R.id.previous_most_probable_chord_type);
        previousMostProbableChordNoteText.setTextColor(colorWhite);
        previousMostProbableChordNoteText.setText(NotesEnum.A.toString());
        previousMostProbableChordTypeText.setTextColor(colorWhite);
        previousMostProbableChordTypeText.setText(ChordTypeEnum.Major.toString());

        // CHORD SCORE
        chordScoreLayout = this.findViewById(R.id.chord_score_layout);

        // TUNING
        tuningPitchNote = this.findViewById(R.id.tuning_pitch_note);
        tuningPitchNote.setTextColor(colorWhite);
        tuningPitchNote.setText(NotesEnum.NO_NOTE.toString());
        tuningPitchNoteLayout = this.findViewById(R.id.tuning_pitch_note_layout);
        tuningPitchNoteTopArrow = this.findViewById(R.id.tuning_pitch_note_top_arrow);
        tuningPitchNoteLeftArrow = this.findViewById(R.id.tuning_pitch_note_left_arrow);
        tuningPitchNoteRightArrow = this.findViewById(R.id.tuning_pitch_note_right_arrow);

        algorithmPerformanceText = this.findViewById(R.id.algorithm_performance);
        algorithmPerformanceText.setTextColor(colorWhite);

        musicPlayingDetectorImageView = this.findViewById(R.id.music_playing_detector);
        musicPlayingDetectorImageView.setVisibility(View.VISIBLE);
        musicPlayingDetectorImageView.setImageResource(R.drawable.ear_24dp);

        versionNumberTextView = this.findViewById(R.id.version_number_text);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "v" + pInfo.versionName;
            versionNumberTextView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        chartFloatingMenu = new ChartFloatingMenu(this, this, colorPrimary, colorPrimaryDark);
        chartFloatingMenu.createChartMenuFloatingMenu();
        functionalitiesFloatingMenu = new FunctionalitiesFloatingMenu(this, this, colorPrimary, colorPrimaryDark);
        functionalitiesFloatingMenu.createChartMenuFloatingMenu();

        setDebugModeViews();
        setFunctionality();

        setChordScoreViews();
        setCanvas();

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.l("EversongActivityLog:: recordingButton pressed!");
                if (!keepRecordingAudio) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
    }

    private void startProcessingFrames() {
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
    }

    private void setChordScoreViews() {
        chordScoreTonicNotePicker = this.findViewById(R.id.tonic_note_picker);
        chordScoreTonicNotePicker.setTextColor(colorWhite);
        chordScoreTonicNotePicker.setSelectedTextColor(colorWhite);
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
        chordScoreChordTypePicker.setTextColor(colorWhite);
        chordScoreChordTypePicker.setSelectedTextColor(colorWhite);
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
        ImageView canvasView = this.findViewById(R.id.canvas_view);
        int canvasHeight = Utils.getActivityHeightInPixels(this) - (
                + (int)Utils.convertDpToPixel(56, this) //Toolbar
                + (int)Utils.convertDpToPixel(24, this) //Top menu (where time, battery... are displayed)
        );

        int canvasWidth = Utils.getActivityWidthInPixels(this);
        ConstraintLayout.LayoutParams params;
        params = (ConstraintLayout.LayoutParams) canvasView.getLayoutParams();
        params.height = canvasHeight;
        canvasView.setLayoutParams(params);
        canvas = new EversongCanvas(getResources(), canvasView, canvasWidth, canvasHeight);
    }

    private void startRecording() {
        recordingButton.setImageResource(R.drawable.baseline_mic_white_24);
        sendBroadcastToService(BroadcastMessage.START_RECORDING_AUDIO);
        detectedChordsFile.startTime = System.currentTimeMillis();
        arrayOfChordsDetected.clear();
        keepRecordingAudio = true;
    }

    private void stopRecording() {
        recordingButton.setImageResource(R.drawable.baseline_mic_off_white_24);
        sendBroadcastToService(BroadcastMessage.STOP_RECORDING_AUDIO);
        keepRecordingAudio = false;
    }

    private void setDebugModeViews() {
        chordNoteText = this.findViewById(R.id.chord_note);
        chordTypeText = this.findViewById(R.id.chord_type);
        chordProbabilityText = this.findViewById(R.id.chord_probability);
        pitchText = this.findViewById(R.id.pitch_text);
        spectralFlatnessText = this.findViewById(R.id.spectral_flatness_text);

        if (Parameters.isDebugMode()) {
            chordNoteText.setVisibility(View.VISIBLE);
            chordTypeText.setVisibility(View.VISIBLE);
            chordProbabilityText.setVisibility(View.VISIBLE);
            pitchText.setVisibility(View.VISIBLE);
            spectralFlatnessText.setVisibility(View.VISIBLE);

            chordNoteText.setTextColor(colorWhite);
            chordTypeText.setTextColor(colorWhite);
            chordProbabilityText.setTextColor(colorWhite);
            pitchText.setTextColor(colorWhite);
            spectralFlatnessText.setTextColor(colorWhite);
        } else {
            chordNoteText.setVisibility(View.GONE);
            chordTypeText.setVisibility(View.GONE);
            chordProbabilityText.setVisibility(View.GONE);
            pitchText.setVisibility(View.GONE);
            spectralFlatnessText.setVisibility(View.GONE);
        }
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    public void processFrame() {
        if (canvas.getCanvas() != null) {
            if (keepRecordingAudio) {
                canvas.updateCanvas(audioSamplesBuffer, audioSpectrumBuffer, AudioStack.getAverageLevel(audioSpectrumBuffer),
                        pitchDetected, chromagram, chordDetected);
            } else {
                canvas.updateCanvas(new double[Parameters.BUFFER_SIZE], audioSpectrumBuffer, 0.0,
                        pitchDetected, new double[NotesEnum.numberOfNotes], chordDetected);
            }
        }

        if (Parameters.isDebugMode()) {
            updateDebugModeViews();
        }

        updateMusicPlayingDetector();

        switch (Parameters.getFunctionalitySelected()) {
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

    public void updateMusicPlayingDetector() {
        musicPlayingDetectorImageView.setVisibility(View.VISIBLE);
        if (musicBeingPlayed) {
            musicPlayingDetectorImageView.setImageResource(R.drawable.ear_24dp);
            musicPlayingDetectorImageView.setAlpha(1f);
            if (polytonalMusicBeingPlayed) {
                Log.l("EversongActivityLog:: Polytonal music being played");
            } else {
                Log.l("EversongActivityLog:: Monotonal music being played");
            }
        } else {
            musicPlayingDetectorImageView.setImageResource(R.drawable.ear_deaf_24dp);
            musicPlayingDetectorImageView.setAlpha(0.5f);
        }
    }

    private void updateChordDetectionViews() {
        float alpha = ((float)mostProbableChord[2] / 100f) > 0.5 ? ((float)mostProbableChord[2] / 100f) : 0.5f;
        boolean isSolfegeMusicalNotation = Parameters.getMusicalNotation() == MusicalNotationEnum.SOLFEGE_NOTATION;
        if (mostProbableChord[0] != -1 && mostProbableChord[1] != -1) {
            if (mostProbableChordNoteText.getVisibility() == View.VISIBLE) {
                mostProbableChordNoteText.setText(NotesEnum.fromInteger(mostProbableChord[0]).toString());
                mostProbableChordNoteText.setAlpha(alpha);
                if (isSolfegeMusicalNotation) {
                    mostProbableChordNoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);
                } else {
                    mostProbableChordNoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 90);
                }
            }
            if (mostProbableChordTypeText.getVisibility() == View.VISIBLE) {
                mostProbableChordTypeText.setText(ChordTypeEnum.fromInteger(mostProbableChord[1]).toString());
                mostProbableChordTypeText.setAlpha(alpha);
            }

            if (arrayOfChordsDetected.size() > 1) {
                int arrayOfChordsDetectedCurrentIndex = arrayOfChordsDetected.size();
                String[] chordArray = Utils.parseChordString(TestAlgorithm.parseChordFromChordElement(arrayOfChordsDetected.get(arrayOfChordsDetectedCurrentIndex - 2)));
                if (previousMostProbableChordNoteText.getVisibility() == View.VISIBLE) {
                    previousMostProbableChordNoteText.setText(chordArray[0]);
                    previousMostProbableChordNoteText.setAlpha(alpha);
                    if (isSolfegeMusicalNotation) {
                        previousMostProbableChordNoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                    } else {
                        previousMostProbableChordNoteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    }
                }
                if (previousMostProbableChordTypeText.getVisibility() == View.VISIBLE) {
                    previousMostProbableChordTypeText.setText(chordArray[1]);
                    previousMostProbableChordTypeText.setAlpha(alpha);
                }
            }
        }
        switch(Parameters.getChartTabSelected()) {
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
        if (musicBeingPlayed && Utils.compareChords(chordScoreTonicNotePicked, chordScoreChordTypePicked,
                NotesEnum.fromInteger(mostProbableChord[0]), ChordTypeEnum.fromInteger(mostProbableChord[1]))) {
            this.findViewById(R.id.chord_score_green_tick).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.chord_score_green_tick).setVisibility(View.GONE);
        }

        switch(Parameters.getChartTabSelected()) {
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
        tuningPitchNote.setText(pitchNote.toString());

        switch(Parameters.getChartTabSelected()) {
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
            pitchText.setText(String.valueOf("Pitch: " + pitchNote.toString()));
        }

        if (chordDetected[0] != -1) {
            chordNoteText.setText(NotesEnum.fromInteger(chordDetected[0]).toString());
        }

        if (chordDetected[1] != -1) {
            chordTypeText.setText(ChordTypeEnum.fromInteger(chordDetected[1]).toString());
        }

        if (chordDetectedProbability != -1) {
            chordProbabilityText.setText(chordDetectedProbability + "%");
        }

        spectralFlatnessText.setText("Flatness: 0." + ((int)(spectralFlatnessValue * 1000000)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dropdown_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.open_settings_menu_option:
                sendBroadcastToService(BroadcastMessage.PAUSE_ACTIVITY);
                keepProcessingFrame = false;
                this.onPause();
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.open_credits:
                sendBroadcastToService(BroadcastMessage.PAUSE_ACTIVITY);
                keepProcessingFrame = false;
                this.onPause();
                intent = new Intent(this, CreditsActivity.class);
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
                        chordDetectedProbability = extras.getFloat(BroadcastExtra.CHORD_DETECTED_PROBABILITY.toString());
                        mostProbableChord = extras.getIntArray(BroadcastExtra.MOST_PROBABLE_CHORD.toString());
                        mostProbableChordBuffer = (int[][])extras.getSerializable(BroadcastExtra.MOST_PROBABLE_CHORD_BUFFER.toString());
                        arrayOfChordsDetected = extras.getStringArrayList(BroadcastExtra.ARRAY_OF_CHORD_DETECTED.toString());
                    } else {
                        Log.l("EversongActivityLog:: Unknown broadcast received");
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void registerEversongActivityBroadcastReceiver() {
        eversongBroadcastReceiver = new EversongActivityBroadcastReceiver();
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

    public void setFunctionality() {
        switch(Parameters.getFunctionalitySelected()) {
            case CHORD_DETECTION:
            default:
                chordDetectedLayout.setVisibility(View.VISIBLE);
                tuningPitchNoteLayout.setVisibility(View.GONE);
                tuningPitchNote.setVisibility(View.GONE);
                chordScoreLayout.setVisibility(View.GONE);
                break;
            case CHORD_SCORE:
                tuningPitchNoteLayout.setVisibility(View.GONE);
                tuningPitchNote.setVisibility(View.GONE);
                chordDetectedLayout.setVisibility(View.GONE);
                chordScoreLayout.setVisibility(View.VISIBLE);
                break;
            case TUNING:
                tuningPitchNoteLayout.setVisibility(View.VISIBLE);
                tuningPitchNote.setVisibility(View.VISIBLE);
                chordDetectedLayout.setVisibility(View.GONE);
                chordScoreLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void inflateChordDetectedLayout() {
        LinearLayout placeHolder;
        placeHolder = this.findViewById(R.id.chord_detected_layout);
        getLayoutInflater().inflate(R.layout.chord_detected, placeHolder);
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

    public void closeApp() {
        Intent serviceIntent = new Intent(getApplicationContext(), EversongService.class);
        getApplicationContext().stopService(serviceIntent);
        this.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(-1);
    }

    @Override
    protected void onDestroy() {
        Log.l("EversongActivityLog:: onDestroy EversongActivity");
        super.onDestroy();
    }
}