package com.upf.minichain.eversongapp;

public class Parameters {
    private static final Parameters instance = new Parameters();

    private Parameters() {
        musicalNotation = MusicalNotation.SOLFEGE_NOTATION;
    }

    public static Parameters getInstance() {
        return instance;
    }

    /************
     * Constants
     ************/
    public static final int SAMPLE_RATE = 44100;    // The sampling rate (16000, 22050, 44100)
    public static final int BUFFER_SIZE = 8192;     // It must be a power of 2 (2048, 4096, 8192, 16384...)
    public static final int HOP_SIZE = 2048;        // It must be a power of 2
    public static final int CHORD_BUFFER_SIZE = 15;

    /************
     * Variables
     ************/
    private static MusicalNotation musicalNotation;

    public void setMusicalNotation(MusicalNotation notation) {
        musicalNotation = notation;
    }

    public MusicalNotation getMusicalNotation() {
        return musicalNotation;
    }
}