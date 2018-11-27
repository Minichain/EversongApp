package com.upf.minichain.eversongapp;

public final class Parameters {

    static {

    }

    enum MusicalNotation{
        SOLFEGE_NOTATION,
        ENGLISH_NOTATION
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
    private static MusicalNotation musicalNotation = MusicalNotation.SOLFEGE_NOTATION;

    public void setMusicalNotation(MusicalNotation notation) {
        musicalNotation = notation;
    }

    public static MusicalNotation getMusicalNotation() {
        return musicalNotation;
    }
}
