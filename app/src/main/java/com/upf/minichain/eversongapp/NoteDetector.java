package com.upf.minichain.eversongapp;

public class NoteDetector {

    public NoteDetector() {
    }

    public NotesEnum detectNote(double[] buffer) {
        int valueIndex = maxValueIndex(buffer);
        double frequencyValue = indexToFrequency(valueIndex);
        double frequencyDistance = 1000.0;
        NotesEnum noteDetected = NotesEnum.NO_NOTE;
        double tempFrequency = 0.0;
        int i = 0;
        float maxFreqToCheck = 10000f;
        while (tempFrequency < maxFreqToCheck) {
            tempFrequency = Math.pow(NotesEnum.refValue, ((double)i - 49.0)) * NotesEnum.refFreq;
            double newFreqDistance = Math.abs(tempFrequency - frequencyValue);
            if (newFreqDistance < frequencyDistance) {
                frequencyDistance = newFreqDistance;
                noteDetected = NotesEnum.fromInteger((i-1) % NotesEnum.numberOfNotes);
            }
            i++;
        }
        Log.l("AdriHell:: Freq: " + frequencyValue);
        Log.l("AdriHell:: Note detected: " + noteDetected);
        return noteDetected;
    }

    public float detectFrequency(double[] buffer) {
        int valueIndex = maxValueIndex(buffer);
        float frequencyValue = indexToFrequency(valueIndex);
        return frequencyValue;
//        Log.l("AdriHell:: Peak detected. Index: " + valueIndex + ", Freq: " + frequencyValue);
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
        frequencyDetected = ((float)Constants.SAMPLE_RATE / 2f) * ((float)index / (Constants.BUFFER_SIZE / 2f));
        return frequencyDetected;
    }
}
