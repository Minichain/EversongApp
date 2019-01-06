package com.upf.minichain.eversongapp.enums;

import com.upf.minichain.eversongapp.Parameters;

public enum NotesEnum {
    A(0), A_SHARP(1), B(2), C(3), C_SHARP(4), D(5), D_SHARP(6), E(7), F(8), F_SHARP(9), G(10), G_SHARP(11), NO_NOTE(-1);

    private final int value;
    public static int numberOfNotes = 12;
    public static double refValue = Math.pow(2.0, 1.0 / 12.0);
    public static double refFreq = 440.0f;

    NotesEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NotesEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return A;
            case 1:
                return A_SHARP;
            case 2:
                return B;
            case 3:
                return C;
            case 4:
                return C_SHARP;
            case 5:
                return D;
            case 6:
                return D_SHARP;
            case 7:
                return E;
            case 8:
                return F;
            case 9:
                return F_SHARP;
            case 10:
                return G;
            case 11:
                return G_SHARP;
            case -1:
            default:
                return NO_NOTE;
        }
    }

    public static float getFrequency(NotesEnum note) {
        switch(note) {
            case A:
                return 440.0f / 8.0f;
            case A_SHARP:
                return 466.1f / 8.0f;
            case B:
                return 493.8f / 8.0f;
            case C:
                return 523.2f / 8.0f;
            case C_SHARP:
                return 554.3f / 8.0f;
            case D:
                return 587.3f / 8.0f;
            case D_SHARP:
                return 622.2f / 8.0f;
            case E:
                return 659.2f / 8.0f;
            case F:
                return 698.4f / 8.0f;
            case F_SHARP:
                return 739.9f / 8.0f;
            case G:
                return 783.9f / 8.0f;
            case G_SHARP:
                return 830.6f / 8.0f;
        }
        return 0f;
    }

    public static String getString(NotesEnum note) {
        MusicalNotationEnum musicalNotation = Parameters.getInstance().getMusicalNotation();
        if (musicalNotation == MusicalNotationEnum.SOLFEGE_NOTATION) {
            switch(note) {
                case A:
                    return "La";
                case A_SHARP:
                    return "La#";
                case B:
                    return "Si";
                case C:
                    return "Do";
                case C_SHARP:
                    return "Do#";
                case D:
                    return "Re";
                case D_SHARP:
                    return "Re#";
                case E:
                    return "Mi";
                case F:
                    return "Fa";
                case F_SHARP:
                    return "Fa#";
                case G:
                    return "Sol";
                case G_SHARP:
                    return "Sol#";
            }
        } else if (musicalNotation == MusicalNotationEnum.ENGLISH_NOTATION) {
            switch(note) {
                case A:
                    return "A";
                case A_SHARP:
                    return "A#";
                case B:
                    return "B";
                case C:
                    return "C";
                case C_SHARP:
                    return "C#";
                case D:
                    return "D";
                case D_SHARP:
                    return "D#";
                case E:
                    return "E";
                case F:
                    return "F";
                case F_SHARP:
                    return "F#";
                case G:
                    return "G";
                case G_SHARP:
                    return "G#";
            }
        }
        return "---";
    }
}