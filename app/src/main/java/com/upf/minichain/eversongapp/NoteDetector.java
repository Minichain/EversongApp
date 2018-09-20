package com.upf.minichain.eversongapp;

import android.util.Log;

public class NoteDetector {
    String LOG_TAG = "AdriHellLog::";
    final int SAMPLE_RATE = 22050; // The sampling rate

    public NoteDetector() {
    }

    public NotesEnum detectNote(double[] buffer) {
        int valueIndex = maxValueIndex(buffer);
        double frequencyValue = indexToFrequency(valueIndex);
        double frequencyDistance = 1000.0;
        NotesEnum noteDetected = NotesEnum.NO_NOTE;
        double tempFrequency = 0.0;
        int i = 35;
        while (tempFrequency < 20000f) {
            tempFrequency = Math.pow(NotesEnum.refValue, ((double)i - 49.0)) * NotesEnum.refFreq;
            double newFreqDistance = Math.abs(tempFrequency - frequencyValue);
            if (newFreqDistance < frequencyDistance) {
                frequencyDistance = newFreqDistance;
                noteDetected = NotesEnum.fromInteger((i-1) % NotesEnum.numberOfNotes);
            }
            i++;
        }
//        Log.v(LOG_TAG, "AdriHell:: Freq: " + frequencyValue);
//        Log.v(LOG_TAG, "AdriHell:: Note detected: " + noteDetected);
        return noteDetected;
    }

    public float detectFrequency(double[] buffer) {
        int valueIndex = maxValueIndex(buffer);
        float frequencyValue = indexToFrequency(valueIndex);
        return frequencyValue;
//        Log.v(LOG_TAG, "AdriHell:: Peak detected. Index: " + valueIndex + ", Freq: " + frequencyValue);
    }

    public int maxValueIndex(double[] buffer) {
        int maxValueIndex = -1;
        double maxValue = 0.0;
        for (int i = 10; i < (buffer.length / 2); i++) {
            if (Math.abs(buffer[i]) > maxValue) {
                maxValueIndex = i;
                maxValue = Math.abs(buffer[i]);
            }
        }
        return maxValueIndex;
    }

    public float indexToFrequency(int index) {
        float frequencyDetected = 0;
        frequencyDetected = ((float)SAMPLE_RATE / 2f) * ((float)index / (4096f / 2));
        return frequencyDetected;
    }
}
