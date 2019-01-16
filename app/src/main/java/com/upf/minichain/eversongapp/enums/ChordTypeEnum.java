package com.upf.minichain.eversongapp.enums;

public enum ChordTypeEnum {
    Minor, Major, Sus2, Sus4, Dominant7th, Major7th, Minor7th, Diminished5th, Augmented5th, Other;

    public static int numberOfChordTypes = 9;

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
            case -1:
            default:
                return null;
        }
    }

    public static String getString(ChordTypeEnum chordType) {
        if (chordType == null) {
            return null;
        }

        switch(chordType) {
            case Minor:
                return "Minor";
            case Major:
                return "Major";
            case Sus2:
                return "Sus2";
            case Sus4:
                return "Sus4";
            case Dominant7th:
                return "Dominant 7th";
            case Major7th:
                return "Major7th";
            case Minor7th:
                return "Minor7th";
            case Diminished5th:
                return "Diminished 5th";
            case Augmented5th:
                return "Augmented 5th";
        }
        return null;
    }
}
