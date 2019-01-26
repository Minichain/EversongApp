package com.upf.minichain.eversongapp.enums;

public enum MusicalNotationEnum {
    SOLFEGE_NOTATION(0),
    ENGLISH_NOTATION(1);

    private final int value;

    MusicalNotationEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

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
