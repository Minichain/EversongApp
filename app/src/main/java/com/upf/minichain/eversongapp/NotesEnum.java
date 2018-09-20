package com.upf.minichain.eversongapp;

enum NotesEnum {
    A, A_SHARP, B, C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, NO_NOTE;

    public static int numberOfNotes = 12;
    public static double refValue = Math.pow(2.0, 1.0 / 12.0);
    public static double refFreq = 440.0f;

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
        }
        return null;
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
}