package com.upf.minichain.eversongapp;

public final class AudioUtils {

    public static double[] fft(final double[] inputReal, boolean DIRECT) {
        return fft(inputReal, null, DIRECT);
    }

    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        return FFTbase.fft(inputReal, inputImag, DIRECT);
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
        int lowCutOffFreqIndex = (int)((lowCutOffFreq / (float)Constants.SAMPLE_RATE) * Constants.BUFFER_SIZE);
        int highCutOffFreqIndex = (int)((highCutOffFreq / (float)Constants.SAMPLE_RATE) * Constants.BUFFER_SIZE);
        int length = samples.length;
        double[] tempSamples = new double[length];
        samples = removeZeroFrequency(samples);
        float attenuationFactor = 1.2f;
        for (int i = 0; i < length; i++) {
            if (lowCutOffFreqIndex > i){
                tempSamples[i] = samples[i] / (attenuationFactor * (lowCutOffFreqIndex - i));
            } else if (lowCutOffFreqIndex <= i && i <= highCutOffFreqIndex) {
                tempSamples[i] = samples[i];
            } else if (i > highCutOffFreqIndex) {
                tempSamples[i] = samples[i] / (attenuationFactor * (i - highCutOffFreqIndex));
            }
        }
        return tempSamples;
    }

    public static double[] removeZeroFrequency(double[] samples) {
        samples[0] = 0;
        samples[1] = 0;
        samples[2] = 0;
        return samples;
    }

    public static double getAverageLevel(double[] samples) {
        double sumOfSamples = 0;
        for (int i = 0; i < samples.length; i++) {
            sumOfSamples += Math.abs(samples[i]);
        }
        return (sumOfSamples / (double)samples.length);
    }

    public static double[] smoothFunction(double[] function) {
        return smoothFunction(function, 2);
    }

    public static double[] smoothFunction(double[] function, int smoothingFactor) {
        double[] outputFunction =  new double[function.length];
        for (int i = smoothingFactor; i < (function.length - smoothingFactor); i++) {
            double average = 0;
            for (int z = i - smoothingFactor; z <= i + smoothingFactor; z++) {
                average += Math.abs(function[z]);
            }
            average = average / (double)(smoothingFactor * 2 + 1);
            outputFunction[i] = average;
        }
        return outputFunction;
    }
}
