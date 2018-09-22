package com.upf.minichain.eversongapp;

public final class AudioUtils extends FFTbase {

    public static double[] fft(final double[] inputReal, boolean DIRECT) {
        return fft(inputReal, null, DIRECT);
    }

    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        return FFTbase.fft(inputReal, inputImag, DIRECT);
    }
}
