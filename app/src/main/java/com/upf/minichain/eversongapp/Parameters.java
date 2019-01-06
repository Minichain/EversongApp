package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class Parameters {
    private static final Parameters instance = new Parameters();

    private Parameters() {
        musicalNotation = MusicalNotationEnum.ENGLISH_NOTATION;
        windowingFunction = WindowFunctionEnum.HAMMING_WINDOW;
        chordBufferSize = 5;
        tabSelected = TabSelected.MAIN;
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

    /************
     * Variables
     ************/
    private static MusicalNotationEnum musicalNotation;
    private static WindowFunctionEnum windowingFunction;
    private static int chordBufferSize;
    enum TabSelected{
        MAIN, CHROMAGRAM
    }
    private static TabSelected tabSelected;

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

    public void setChordBufferSize(int newSize) {
        chordBufferSize = newSize;
    }

    public int getChordBufferSize() {
        return chordBufferSize;
    }

    public void setTabSelected(TabSelected tab) {
        tabSelected = tab;
    }

    public TabSelected getTabSelected() {
        return tabSelected;
    }
}
