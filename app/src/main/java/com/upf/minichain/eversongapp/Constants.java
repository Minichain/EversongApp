package com.upf.minichain.eversongapp;

public class Constants {
    private static Constants instance;

    public Constants() {

    }

    public Constants getInstance() {
        return instance;
    }

    public static final int SAMPLE_RATE = 22050;  // The sampling rate
    public static final int BUFFER_SIZE = 4096;   // Power of 2
}
