package com.upf.minichain.eversongapp;

public class NoteDetector {

    public NoteDetector() {
    }

    public NotesEnum detectNote(double frequencyValue) {
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
                noteDetected = NotesEnum.fromInteger(((i-1) + 3) % NotesEnum.numberOfNotes);
            }
            i++;
        }
//        Log.l("AdriHell:: Freq: " + frequencyValue);
//        Log.l("AdriHell:: Note detected: " + noteDetected);
        return noteDetected;
    }

    public float detectFrequency(double[] buffer, double threshold) {
        int valueIndex = maxValueIndex(buffer, threshold);
        if (valueIndex != -1) {
            return indexToFrequency(valueIndex);
        } else {
            return -1;
        }
    }

    public int maxValueIndex(double[] buffer, double threshold) {
        int maxValueIndex = -1;
        double maxValue = 0.0;
        for (int i = 0; i < (buffer.length / 2); i++) {
            if (buffer[i] >= threshold && buffer[i] > maxValue) {
                maxValueIndex = i;
                maxValue = buffer[i];
            }
        }
        return maxValueIndex;
    }

    public int[] detectPeaks(double[] buffer, int numOfPeaksToDetect, double threshold) {
        int[] peaksDetected = new int[numOfPeaksToDetect];
        int peaksDetectedCounter = 0;
        int i = 0;
        while (peaksDetectedCounter < numOfPeaksToDetect && i < ((buffer.length / 2) - 1)) {
            if (buffer[i] >= threshold
                    && buffer[i - 1] < buffer[i]
                    && buffer[i] > buffer[i + 1]) {
                peaksDetected[peaksDetectedCounter] = i;
                peaksDetectedCounter++;
            }
            i++;
        }
        return peaksDetected;
    }

    public float indexToFrequency(int index) {
        float frequencyDetected;
        frequencyDetected = ((float)Constants.SAMPLE_RATE / 2f) * ((float)index / (Constants.BUFFER_SIZE / 2f));
        return frequencyDetected;
    }
}
