package com.upf.minichain.eversongapp;

public enum MusicalNotation {
    SOLFEGE_NOTATION,
    ENGLISH_NOTATION;

    public static String getString(MusicalNotation note) {
        switch(note) {
            case ENGLISH_NOTATION:
                return "English notation (A, B, C...)";
            case SOLFEGE_NOTATION:
            default:
                return "Solfège notation (La, Si...)";
        }
    }
}
