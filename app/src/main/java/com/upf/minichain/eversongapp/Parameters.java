package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class Parameters {
    private static final Parameters instance = new Parameters();

    private Parameters() {
        musicalNotation = MusicalNotationEnum.SOLFEGE_NOTATION;
        windowingFunction = WindowFunctionEnum.HANNING_WINDOW;
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
    private static MusicalNotationEnum musicalNotation;
    private static WindowFunctionEnum windowingFunction;

    public void setMusicalNotation(MusicalNotationEnum notation) {
        musicalNotation = notation;
    }

    public MusicalNotationEnum getMusicalNotation() {
        return musicalNotation;
    }

    public void setWindowingFunction(WindowFunctionEnum window) {
        windowingFunction = window;
    }

    public WindowFunctionEnum getWindowingFunction() {
        return windowingFunction;
    }
}
