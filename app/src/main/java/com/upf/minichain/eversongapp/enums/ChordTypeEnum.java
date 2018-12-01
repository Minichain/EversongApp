package com.upf.minichain.eversongapp.enums;

public enum ChordTypeEnum {
    Minor, Major, Suspended, Dominant, Dimished5th, Augmented5th, Other;

    public static int numberOfChordTypes = 6;

    public static ChordTypeEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return Minor;
            case 1:
                return Major;
            case 2:
                return Suspended;
            case 3:
                return Dominant;
            case 4:
                return Dimished5th;
            case 5:
                return Augmented5th;
        }
        return null;
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
            case Suspended:
                return "Suspended";
            case Dominant:
                return "5th";
            case Dimished5th:
                return "Dimished 5th";
            case Augmented5th:
                return "Augmented 5th";
        }
        return null;
    }
}
