package com.upf.minichain.eversongapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

/**
 * Utils class. Only "static" methods.
 */
public class Utils {
    public static float getAverage(float[] array, int length) {
        float sum = 0;
        for (int i = 0; i < length; i++) {
            sum += array[i];
        }
        return (sum / length);
    }

    public static double getAverage(double[] array, int length) {
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += array[i];
        }
        return (sum / length);
    }

    public static float getStandardDeviation(float[] array, int length) {
        float sum = 0;
        float mean = getAverage(array, length);
        for (int i = 0; i < length; i++) {
            sum += Math.pow(array[i] - mean, 2);
        }
        return (sum / (length - 1));
    }

    /**
     * This method takes an hexadecimal color as an input ("FCFCFCFC" or "FCFCFC") and
     * returns an array of 3 integers which are the colour in RGB (252, 252, 252).
     * */
    public static int[] hexadecimalToRgb(String hex) {
        hex = hex.replace("#", "");
        if (hex.length() == 8) {
            hex = hex.substring(2, 8);  //Transforms "FCFCFCFC" into "FCFCFC"
        }
        final int[] ret = new int[3];
        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        return px / (float) context.getResources().getDisplayMetrics().densityDpi;
    }

    public static float convertSpToPixels(float sp, Context context) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static float convertPixelsToSp(float px, Context context) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * This method takes a chord as an input [G, major]
     * and returns the notes that compose the chord [G, B, D].
     * */
    public static NotesEnum[] getChordNotes(NotesEnum tonic, ChordTypeEnum chordType) {
        NotesEnum[] chordNotes = new NotesEnum[4];
        chordNotes[0] = tonic;
        chordNotes[1] = NotesEnum.NO_NOTE;
        chordNotes[2] = NotesEnum.NO_NOTE;
        chordNotes[3] = NotesEnum.NO_NOTE;

        switch(chordType) {
            case Major:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 4) % NotesEnum.numberOfNotes);    //The major third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                break;
            case Minor:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 3) % NotesEnum.numberOfNotes);    //The minor third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                break;
            case Dominant7th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 4) % NotesEnum.numberOfNotes);    //The major third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                chordNotes[3] = NotesEnum.fromInteger((tonic.getValue() + 10) % NotesEnum.numberOfNotes);   //The seventh
                break;
            case Sus2:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 2) % NotesEnum.numberOfNotes);    //The second
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                break;
            case Sus4:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 5) % NotesEnum.numberOfNotes);    //The fourth
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                break;
            case Major7th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 4) % NotesEnum.numberOfNotes);    //The major third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                chordNotes[3] = NotesEnum.fromInteger((tonic.getValue() + 11) % NotesEnum.numberOfNotes);   //The seventh
                break;
            case Minor7th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 3) % NotesEnum.numberOfNotes);    //The minor third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The fifth
                chordNotes[3] = NotesEnum.fromInteger((tonic.getValue() + 10) % NotesEnum.numberOfNotes);   //The dominant seventh
                break;
            case Diminished5th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 3) % NotesEnum.numberOfNotes);    //The minor third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 6) % NotesEnum.numberOfNotes);    //The diminished fifth
                break;
            case Augmented5th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 4) % NotesEnum.numberOfNotes);    //The major third
                chordNotes[2] = NotesEnum.fromInteger((tonic.getValue() + 8) % NotesEnum.numberOfNotes);    //The augmented fifth
                break;
            case Power5th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 7) % NotesEnum.numberOfNotes);    //The major third
                break;
            case NoChord:
            default:
                break;
        }
        return chordNotes;
    }

    public static int getActivityWidthInPixels(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getActivityHeightInPixels(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * This method compares chords note by note. If both chords have the same notes, even if they
     * are not the same chord, it will return true.
     * Example: "compareChords(Dsus4, Gsus2)" returns true.
     * */
    public static boolean compareChords(NotesEnum tonic1, ChordTypeEnum chordType1, NotesEnum tonic2, ChordTypeEnum chordType2) {
        NotesEnum[] chord1Notes = getChordNotes(tonic1, chordType1);
        NotesEnum[] chord2Notes = getChordNotes(tonic2, chordType2);
        NotesEnum note;
        boolean noteInChord;
        for (int i = 0; i < 4; i++) {
            note = chord1Notes[i];
            noteInChord = false;
            for (int j = 0; j < 4; j++) {
                if (note == chord2Notes[j]) {
                    noteInChord = true;
                    break;
                }
            }
            if (!noteInChord) {
                return false;
            }
        }
        return true;
    }

    public static String[] parseChordString(String chordString) {
        String chordArray[] = new String[2];
        int position = chordString.indexOf(" ");
        chordArray[0] = chordString.substring(0, position);
        chordArray[1] = chordString.substring(position, chordString.length());
        return chordArray;
    }
}
