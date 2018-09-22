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

    public static double[] bandPassFilter(double[] samples, float lowFreq, float high) {
        //TODO
        return samples;
    }
}
