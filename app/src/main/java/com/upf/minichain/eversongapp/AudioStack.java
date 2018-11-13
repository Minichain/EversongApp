package com.upf.minichain.eversongapp;

public final class AudioStack {

    static {
        System.loadLibrary("native-lib");
    }

    public static void initAudioStack() {
        initProcessAudioJni(Constants.SAMPLE_RATE, Constants.BUFFER_SIZE, Constants.HOP_SIZE);
    }

    public static int[] chordDetection(double[] samples, double[] spectrumSamples) {
        return chordDetectionJni(samples, spectrumSamples);
    }

    public static float getPitch(double[] samples) {
        return getPitchJni(samples);
    }

    public static double[] fft(double[] inputReal, boolean DIRECT) {
        return fftJni(inputReal, DIRECT);
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
        return bandPassFilterJni(samples, lowCutOffFreq, highCutOffFreq, Constants.SAMPLE_RATE, Constants.BUFFER_SIZE);
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
//        Log.l("AdriHell:: Freq: " + frequencyValue);
//        Log.l("AdriHell:: Note detected: " + noteDetected);
        return noteDetected;
    }

    public static float getFrequencyPeak(double[] buffer, double threshold) {
        int valueIndex = getMaxValueIndex(buffer, threshold);
        if (valueIndex != -1) {
            return getFrequencyByIndex(valueIndex);
        } else {
            return -1;
        }
    }

    public static int getMaxValueIndex(double[] buffer, double threshold) {
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
        float frequencyDetected;
        frequencyDetected = ((float)Constants.SAMPLE_RATE / 2f) * ((float)index / (Constants.BUFFER_SIZE / 2f));
        return frequencyDetected;
    }

    /**
     * This methods are used in order
     * to access to the C++ implementation
     **/
    private static native void initProcessAudioJni(int sample_rate, int frame_size, int hop_size);

    private static native int[] chordDetectionJni(double[] samples, double[] spectrumSamples);

    private static native double[] fftJni(double[] inputReal, boolean DIRECT);

    private static native double[] bandPassFilterJni(double[] spectrumSamples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);

    private static native double getAverageLevelJni(double[] samples);

    private static native float getPitchJni(double[] samples);
}
