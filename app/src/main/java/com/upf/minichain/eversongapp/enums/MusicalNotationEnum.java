package com.upf.minichain.eversongapp.enums;

public enum MusicalNotationEnum {
    SOLFEGE_NOTATION,
    ENGLISH_NOTATION;

    public static String getString(MusicalNotationEnum note) {
        switch(note) {
            case ENGLISH_NOTATION:
                return "English notation (A, B, C...)";
            case SOLFEGE_NOTATION:
            default:
                return "Solfège notation (La, Si...)";
        }
    }
}
