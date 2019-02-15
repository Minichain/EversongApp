package com.upf.minichain.eversongapp.enums;

public enum ChordTypeEnum {
    Minor(0), Major(1), Sus2(2), Sus4(3), Dominant7th(4), Major7th(5), Minor7th(6), Diminished5th(7), Augmented5th(8), NoChord(9);

    private final int value;
    public static int numberOfChordTypes = 9;

    ChordTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ChordTypeEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return Minor;
            case 1:
                return Major;
            case 2:
                return Sus2;
            case 3:
                return Sus4;
            case 4:
                return Dominant7th;
            case 5:
                return Major7th;
            case 6:
                return Minor7th;
            case 7:
                return Diminished5th;
            case 8:
                return Augmented5th;
            case 9:
            case -1:
            default:
                return NoChord;
        }
    }

    public String toString() {
        switch(this) {
            case Minor:
                return "Minor";
            case Major:
                return "Major";
            case Sus2:
                return "Sus2";
            case Sus4:
                return "Sus4";
            case Dominant7th:
                return "Dominant7th";
            case Major7th:
                return "Major7th";
            case Minor7th:
                return "Minor7th";
            case Diminished5th:
                return "Diminished5th";
            case Augmented5th:
                return "Augmented5th";
            case NoChord:
            default:
                return "";
        }
    }
}
