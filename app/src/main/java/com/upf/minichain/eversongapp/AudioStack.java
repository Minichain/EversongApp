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

    public static double[] removeZeroFrequency(double[] samples) {
        samples[0] = 0;
        samples[1] = 0;
        samples[2] = 0;
        return samples;
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

    /**
     * This methods are used in order
     * to access to the C++ implementation
     **/
    private static native void initProcessAudioJni(int sample_rate, int frame_size, int hop_size);

    private static native int[] chordDetectionJni(double[] samples, double[] spectrumSamples);

    private static native double[] fftJni(double[] inputReal, boolean DIRECT);

    private static native double[] bandPassFilterJni(double[] spectrumSamples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);

    private static native double getAverageLevelJni(double[] samples);
}
