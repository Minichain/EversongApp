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

    public static double[] bandPassFilter(double[] samples, float lowFreq, float highFreq) {
        int lowFreqIndex = (int)((lowFreq / (float)Constants.SAMPLE_RATE) * Constants.BUFFER_SIZE);
        int highFreqIndex = (int)((highFreq / (float)Constants.SAMPLE_RATE) * Constants.BUFFER_SIZE);
        int length = samples.length;
        double[] tempSamples = new double[length];
        samples = removeZeroFrequency(samples);
        float attenuationFactor = 1.2f;
        for (int i = 0; i < length; i++) {
            if (lowFreqIndex > i){
                tempSamples[i] = samples[i] / (float)(attenuationFactor * (lowFreqIndex - i));
            } else if (lowFreqIndex <= i && i <= highFreqIndex) {
                tempSamples[i] = samples[i];
            } else if (i > highFreqIndex) {
                tempSamples[i] = samples[i] / (float)(attenuationFactor * (i - highFreqIndex));
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
}
