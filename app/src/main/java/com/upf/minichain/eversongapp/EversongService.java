package com.upf.minichain.eversongapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.upf.minichain.eversongapp.enums.BroadcastExtra;
import com.upf.minichain.eversongapp.enums.BroadcastMessage;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EversongService extends Service {
    EversongServiceBroadcastReceiver eversongBroadcastReceiver;
    boolean keepRecordingAudio;

    boolean musicBeingPlayed;
    boolean polytonalMusicBeingPlayed;
    float pitchDetected;
    float pitchProbability;
    float[] pitchDetectedBuffer;
    int pitchBufferIterator = 0;
    NotesEnum pitchNote;
    double spectralFlatnessValue;
    int[] chordDetected = new int[2];
    int chordDetectedProbability;
    int[][] mostProbableChordBuffer;
    int chordBufferIterator = 0;
    ArrayList<String> arrayOfChordsDetected;
    DetectedChordFile detectedChordsFile;

    int[] mostProbableChord = new int[3];
    double[] chromagram = new double[NotesEnum.numberOfNotes];

    double[] audioSamplesBuffer;
    double[] audioSamplesBufferWindowed;
    double[] audioSpectrumBuffer;
    double[] prevAudioSpectrumBuffer;

    NotificationManager notificationManager;
    NotificationManagerCompat notificationManagerCompat;
    final static String serviceNotificationStringId = "SERVICE_NOTIFICATION";
    final static int serviceNotificationId = 1;

    @Override
    public void onCreate() {
        eversongBroadcastReceiver = new EversongServiceBroadcastReceiver();
        registerEversongServiceBroadcastReceiver();

        musicBeingPlayed = false;
        polytonalMusicBeingPlayed = false;
        pitchDetected = -1;
        pitchNote = NotesEnum.NO_NOTE;
        chordDetected[0] = -1;
        chordDetected[1] = -1;
        chordDetectedProbability = -1;
        mostProbableChord[0] = NotesEnum.A.getValue();
        mostProbableChord[1] = ChordTypeEnum.Major.getValue();
        mostProbableChord[2] = 100;
        arrayOfChordsDetected =  new ArrayList<>();
        detectedChordsFile = new DetectedChordFile(getApplicationContext());

        createEversongServiceNotification();

        Log.l("EversongServiceLog:: onCreate service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.l("EversongServiceLog:: onStartCommand service");
        AudioStack.initAudioStack();
        pitchDetectedBuffer = new float[Parameters.getInstance().getPitchBufferSize()];
        mostProbableChordBuffer = new int[Parameters.getInstance().getChordBufferSize()][2];

        if (audioSamplesBuffer == null || Parameters.BUFFER_SIZE != audioSamplesBuffer.length) {
            audioSamplesBuffer = new double[Parameters.BUFFER_SIZE];
        }
        if (audioSpectrumBuffer == null || Parameters.BUFFER_SIZE != audioSpectrumBuffer.length) {
            audioSpectrumBuffer = new double[Parameters.BUFFER_SIZE];
        }

        setActivityChordsDetected();
        setActivitySampleBuffer();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.l("EversongServiceLog:: onBind service");
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(eversongBroadcastReceiver);
        removeEversongServiceNotification();
        Log.l("EversongServiceLog:: onDestroy service");
    }

    private void sendBroadcastToActivity(BroadcastMessage broadcastMessage) {
        sendBroadcastToActivity(broadcastMessage, null);
    }

    private void sendBroadcastToActivity(BroadcastMessage broadcastMessage, Bundle extras) {
        Log.l("EversongServiceLog:: sending broadcast " + broadcastMessage.toString());
        try {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(broadcastMessage.toString());
            if (extras != null) {
                broadCastIntent.putExtras(extras);
            }
            sendBroadcast(broadCastIntent);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class EversongServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.l("EversongServiceLog:: Broadcast received " + intent.getAction());
            try {
                String broadcast = intent.getAction();
                if (broadcast != null) {
                    if (broadcast.equals(BroadcastMessage.REFRESH_FRAME.toString())) {
                        Log.l("EversongActivityLog:: refreshing frame!");
                    } else if (broadcast.equals(BroadcastMessage.START_RECORDING_AUDIO.toString())) {
                        detectedChordsFile.startTime = System.currentTimeMillis();
                        arrayOfChordsDetected.clear();
                        recordAudio();
                    } else if (broadcast.equals(BroadcastMessage.STOP_RECORDING_AUDIO.toString())) {
                        keepRecordingAudio = false;
                        if (Parameters.getInstance().isDebugMode()) {
                            Bundle extras = new Bundle();
                            extras.putDouble(BroadcastExtra.ALGORITHM_PERFORMANCE.toString(), TestAlgorithm.computeAlgorithmPerformance(arrayOfChordsDetected));
                            sendBroadcastToActivity(BroadcastMessage.ALGORITHM_PERFORMANCE, extras);
//                            Log.l("AlgorithmPerformanceLog:: Performance: " + (int)(TestAlgorithm.computeAlgorithmPerformance(arrayOfChordsDetected) * 100) + "%");
                        }
                    } else if (broadcast.equals(BroadcastMessage.PAUSE_ACTIVITY.toString())) {
                        keepRecordingAudio = false;
                    } else if (broadcast.equals(BroadcastMessage.DESTROY_EVERSONG_SERVICE.toString())) {
                        onDestroy();
                    } else {

                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void registerEversongServiceBroadcastReceiver() {
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

    public void processAudio() {
        if (musicBeingPlayed) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int[] chordDetectedThread = AudioStack.chordDetection(audioSamplesBufferWindowed, audioSpectrumBuffer, Parameters.getInstance().getChordDetectionAlgorithm().getValue());
                    final double chordDetectedProbabilityThread = AudioStack.getChordProbability();
                    final double[] chromagramThread = AudioStack.getChromagram();

                    chordDetectedProbability = (int)((1 - chordDetectedProbabilityThread) * 100);
                    Log.l("EversongServiceLog:: Chord detected: " + chordDetected[0] + ", " + chordDetected[1] + ", Probability: " + chordDetectedProbability);

                    if (chordDetectedProbability >= Parameters.getInstance().getChordProbabilityThreshold()) {
                        chordDetected = chordDetectedThread;
                    } else {
                        chordDetected[0] = -1;
                        chordDetected[1] = -1;
                    }

                    chromagram = chromagramThread;
                    mostProbableChordBuffer[chordBufferIterator % Parameters.getInstance().getChordBufferSize()][0] = chordDetected[0];
                    mostProbableChordBuffer[chordBufferIterator % Parameters.getInstance().getChordBufferSize()][1] = chordDetected[1];
                    mostProbableChord = AudioStack.getMostProbableChord(mostProbableChordBuffer);
                    chordBufferIterator++;
                    updateDetectedChordsList();
                    setActivityChordsDetected();
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final float pitchDetectedThread = AudioStack.getPitch(audioSamplesBufferWindowed);
                final float pitchProbabilityThread = AudioStack.getPitchProbability();
                if (pitchDetectedThread <= Parameters.BANDPASS_FILTER_LOW_FREQ || Parameters.BANDPASS_FILTER_HIGH_FREQ <= pitchDetectedThread) {
                    pitchDetected = -1;
                    pitchProbability = -1;
                } else {
                    pitchDetected = pitchDetectedThread;
                    pitchProbability = pitchProbabilityThread;
                }

                pitchDetectedBuffer[pitchBufferIterator % Parameters.getInstance().getPitchBufferSize()] = pitchDetected;
                pitchDetected = Utils.getAverage(pitchDetectedBuffer, Parameters.getInstance().getPitchBufferSize());
                float stdDeviation = Utils.getStandardDeviation(pitchDetectedBuffer, Parameters.getInstance().getPitchBufferSize());
                if (stdDeviation > 100) {
                    pitchDetected = -1;
                }
                pitchBufferIterator++;

                Bundle extras = new Bundle();
                extras.putFloat(BroadcastExtra.PITCH_DETECTED.toString(), pitchDetected);
                extras.putFloat(BroadcastExtra.PITCH_PROBABILITY.toString(), pitchProbability);
                sendBroadcastToActivity(BroadcastMessage.PITCH_DETECTION, extras);

//                Log.l("PitchLog:: std deviation: " + stdDeviation);
//                Log.l("PitchLog:: Pitch detected with probability " + pitchProbability);
            }
        }).start();

        spectralFlatnessValue = AudioStack.getSpectralFlatness(Arrays.copyOfRange(audioSpectrumBuffer, 0, Parameters.BUFFER_SIZE / 2));

        //TODO How do we detect if there's music being played??
        if (pitchProbability >= 0.70 && spectralFlatnessValue < 0.999995 && !musicBeingPlayed) {
            polytonalMusicBeingPlayed = (pitchProbability < 0.95) ? true : false;
            musicBeingPlayed = true;
            setActivityMusicDetection();
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.schedule(new Runnable() {
                @Override
                public void run(){
                    musicBeingPlayed = false;
                    setActivityMusicDetection();
                }
            }, 2000, TimeUnit.MILLISECONDS);
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
                    Log.l("EversongActivityLog:: Audio Record cannot be initialized!");
                    return;
                }
                record.startRecording();

                Log.l("EversongActivityLog:: Start recording");

                keepRecordingAudio = true;
                final long startTime = System.currentTimeMillis();

                while (keepRecordingAudio) {
                    numberOfShortRead = record.read(tempAudioSamples, 0, tempAudioSamples.length);
                    totalShortsRead += numberOfShortRead;
                    prevAudioSpectrumBuffer = audioSpectrumBuffer;
                    audioSamplesBuffer = AudioStack.getSamplesToDouble(tempAudioSamples);
                    audioSamplesBufferWindowed = AudioStack.window(audioSamplesBuffer, Parameters.getInstance().getWindowingFunction());
                    audioSpectrumBuffer = AudioStack.bandPassFilter(AudioStack.fft(audioSamplesBufferWindowed, true), Parameters.BANDPASS_FILTER_LOW_FREQ, Parameters.BANDPASS_FILTER_HIGH_FREQ);
                    setActivitySampleBuffer();
                    processAudio();
//                    System.gc();
//                    Log.l("EversongActivityLog:: reading buffer of size " + Parameters.BUFFER_SIZE + ", Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
                }
                record.stop();
                record.release();

                Log.l("EversongActivityLog:: Recording stopped. Num of samples read: " + totalShortsRead);
            }
        }).start();
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

    private void setActivityChordsDetected() {
        Bundle extras = new Bundle();
        extras.putDoubleArray(BroadcastExtra.CHROMAGRAM.toString(), chromagram);
        extras.putIntArray(BroadcastExtra.CHORD_DETECTED.toString(), chordDetected);
        extras.putInt(BroadcastExtra.CHORD_DETECTED_PROBABILITY.toString(), chordDetectedProbability);
        extras.putIntArray(BroadcastExtra.MOST_PROBABLE_CHORD.toString(), mostProbableChord);
        extras.putSerializable(BroadcastExtra.MOST_PROBABLE_CHORD_BUFFER.toString(), mostProbableChordBuffer);
        sendBroadcastToActivity(BroadcastMessage.CHORD_DETECTION_PROCESSED, extras);
    }

    private void setActivitySampleBuffer() {
        Bundle extras = new Bundle();
        extras.putDoubleArray(BroadcastExtra.AUDIO_SAMPLES_BUFFER.toString(), audioSamplesBuffer);
        extras.putDoubleArray(BroadcastExtra.AUDIO_SPECTRUM_BUFFER.toString(), audioSpectrumBuffer);
        sendBroadcastToActivity(BroadcastMessage.AUDIO_CAPTURED, extras);
    }

    private void setActivityMusicDetection() {
        Bundle extras = new Bundle();
        extras.putBoolean(BroadcastExtra.MUSIC_BEING_PLAYED.toString(), musicBeingPlayed);
        extras.putBoolean(BroadcastExtra.POLYTONAL_MUSIC_BEING_PLAYED.toString(), polytonalMusicBeingPlayed);
        extras.putDouble(BroadcastExtra.SPECTRAL_FLATNESS.toString(), spectralFlatnessValue);
        sendBroadcastToActivity(BroadcastMessage.MUSIC_DETECTION, extras);
    }

    private void createEversongServiceNotification() {
        //Service notification
        CharSequence name = getResources().getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(serviceNotificationStringId, name, importance);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //Notification intent to open the activity when pressing the notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, EversongActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, serviceNotificationStringId)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(name)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
//                .setContentIntent(contentIntent);

        notificationManagerCompat = NotificationManagerCompat.from(this);

        Notification notification = builder.build();
        notificationManagerCompat.notify(serviceNotificationId, notification);
        this.startForeground(1, notification);
    }

    private void removeEversongServiceNotification() {
        if (notificationManagerCompat != null) {
            notificationManagerCompat.cancel(serviceNotificationId);
        }
    }
}
