package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public final class AudioStack {

    static {
        System.loadLibrary("native-lib");
    }

    public static void initAudioStack() {
        initProcessAudioJni(Parameters.SAMPLE_RATE, Parameters.BUFFER_SIZE, Parameters.HOP_SIZE);
    }

    public static int[] chordDetection(double[] samples, double[] spectrumSamples) {
        return chordDetectionJni(samples, spectrumSamples);
    }

    public static float getPitch(double[] samples) {
        return getPitchJni(samples);
    }

    public static double[] fft(double[] inputReal, boolean DIRECT, WindowFunctionEnum windowFunction) {
        return fftJni(inputReal, DIRECT, windowFunction.getIntValue());
    }

    public static double[] highPassFilter(double[] samples, float cutOffFreq) {
        //TODO
        return samples;
    }

    public static double[] lowPassFilter(double[] samples, float cutOffFreq) {
        //TODO
        return samples;
    }

    public static double[] bandPassFilter(double[] samples, float lowCutOffFreq, float highCutOffFreq) {
        return bandPassFilterJni(samples, lowCutOffFreq, highCutOffFreq, Parameters.SAMPLE_RATE, Parameters.BUFFER_SIZE);
    }

    public static double getAverageLevel(double[] samples) {
        return getAverageLevelJni(samples);
    }

    public static double[] smoothFunction(double[] function) {
        return smoothFunction(function, 2);
    }

    public static double[] smoothFunction(double[] function, int smoothingFactor) {
        double[] outputFunction =  new double[function.length];
        for (int i = smoothingFactor; i < (function.length - smoothingFactor); i++) {
            double average = 0;
            for (int z = i - smoothingFactor; z <= i + smoothingFactor; z++) {
                average += function[z];
            }
            average = average / (double)(smoothingFactor * 2 + 1);
            outputFunction[i] = average;
        }
        return outputFunction;
    }

    public static NotesEnum getNoteByFrequency(double frequencyValue) {
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
                noteDetected = NotesEnum.fromInteger(((i - 4)) % NotesEnum.numberOfNotes);
            }
            i++;
        }
        return noteDetected;
    }

    public static float getFrequencyPeak(double[] spectrum, double threshold) {
        int valueIndex = getMaxSpectrumValueIndex(spectrum, threshold);
        if (valueIndex != -1) {
            return getFrequencyByIndex(valueIndex);
        } else {
            return -1;
        }
    }

    public static int getMaxSpectrumValueIndex(double[] buffer, double threshold) {
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

    public static float getFrequencyByIndex(int index) {
        return (float)Parameters.SAMPLE_RATE * ((float)index / Parameters.BUFFER_SIZE);
    }

    public static int getIndexByFrequency(float freq) {
        return (int)((freq * Parameters.BUFFER_SIZE * 2) / Parameters.SAMPLE_RATE);
    }

    /**
     * This function returns the most probable chord from the list of
     * chords stored in the ChordBuffer with size = CHORD_BUFFER_SIZE
     * */
    public static int[] getMostProbableChord(int[][] chordsDetectedBuffer) {
        int[] mostProbableChord = new int[3];
        mostProbableChord[0] = -1;
        mostProbableChord[1] = -1;

        int[] rootNoteArray = new int[NotesEnum.numberOfNotes];
        int[] chordTypeArray = new int[ChordTypeEnum.numberOfChordTypes];

        for(int i = 0; i < Parameters.CHORD_BUFFER_SIZE; i++) {
            int rootNote = chordsDetectedBuffer[i][0] % NotesEnum.numberOfNotes;
            int chordType = chordsDetectedBuffer[i][1] % ChordTypeEnum.numberOfChordTypes;
            if (rootNote != -1) {
                rootNoteArray[rootNote]++;
            }
            if (chordType != -1) {
                chordTypeArray[chordType]++;
            }
        }
        int[] rootNoteProbability = getMaxValueAndIndex(rootNoteArray);
        mostProbableChord[0] = rootNoteProbability[1];
        mostProbableChord[1] = getMaxValueAndIndex(chordTypeArray)[1];
        mostProbableChord[2] = (rootNoteProbability[0] * 100) / Parameters.CHORD_BUFFER_SIZE;
        return mostProbableChord;
    }

    public static int[] getMaxValueAndIndex(int[] buffer) {
        int[] returnValue = new int[2];
        returnValue[0] = 0;     //value
        returnValue[1] = -1;    //index
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > returnValue[0]) {
                returnValue[0] = buffer[i];
                returnValue[1] = i;
            }
        }
        return returnValue;
    }

    /**
     * This methods are used in order
     * to access to the C++ implementation
     **/
    private static native void initProcessAudioJni(int sample_rate, int frame_size, int hop_size);

    private static native int[] chordDetectionJni(double[] samples, double[] spectrumSamples);

    private static native double[] fftJni(double[] inputReal, boolean DIRECT, int windowFunction);

    private static native double[] bandPassFilterJni(double[] spectrumSamples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);

    private static native double getAverageLevelJni(double[] samples);

    private static native float getPitchJni(double[] samples);
}
