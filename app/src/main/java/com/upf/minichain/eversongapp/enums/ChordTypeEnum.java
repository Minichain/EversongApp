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
}
