package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class Parameters {
    private static final Parameters instance = new Parameters();

    public static Parameters getInstance() {
        return instance;
    }

    /************
     * Constants
     ************/
    public static final int SAMPLE_RATE = 44100;    // The sampling rate (16000, 22050, 44100)
    public static final int BUFFER_SIZE = 8192;     // It must be a power of 2 (2048, 4096, 8192, 16384...)
    public static final int HOP_SIZE = 2048;        // It must be a power of 2
    public static final int FRAMES_PER_SECOND = 10;

    /************
     * Variables
     ************/
    private static MusicalNotationEnum musicalNotation;
    private static WindowFunctionEnum windowingFunction;
    private static int chordBufferSize;
    enum TabSelected {
        GUITAR_TAB, UKULELE_TAB, CHROMAGRAM
    }
    private static TabSelected tabSelected;
    private static boolean debugMode;

    private Parameters() {
        musicalNotation = MusicalNotationEnum.ENGLISH_NOTATION;
        windowingFunction = WindowFunctionEnum.HAMMING_WINDOW;
        chordBufferSize = 7;
        tabSelected = TabSelected.GUITAR_TAB;
        debugMode = false;
    }

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

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
