package com.upf.minichain.eversongapp;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

/**
 * This class is designed to retrieve the guitar tabs of a certain chord.
 * For example...
 *      for the chord "A minor" it would be {0, 0, 2, 2, 1, 0}
 *      for the chord "C major" it would be {0, 3, 2, 0, 1, 0}
 *      for the chord "F major" it would be {1, 3, 3, 2, 1, 1}
 */
public class GuitarChordChart {
    private static int numberOfStrings = 6;
    private static int numberOfFrets = 4;
    private static NotesEnum[] guitarTuning = new NotesEnum[numberOfStrings];

    static {
        guitarTuning[0] = NotesEnum.E;
        guitarTuning[1] = NotesEnum.A;
        guitarTuning[2] = NotesEnum.D;
        guitarTuning[3] = NotesEnum.G;
        guitarTuning[4] = NotesEnum.B;
        guitarTuning[5] = NotesEnum.E;
    }

    public static int[] getChordTab(NotesEnum tonic, ChordTypeEnum chordType) {
        int[] chordTab = new int[numberOfStrings];
        chordTab[0] = -1;
        chordTab[1] = -1;
        chordTab[2] = -1;
        chordTab[3] = -1;
        chordTab[4] = -1;
        chordTab[5] = -1;

        NotesEnum[] chordNotes = getChordNotes(tonic, chordType);
        Log.l("GuitarChordChartLog:: The chord is: " + NotesEnum.getString(tonic) + " " + chordType);
        Log.l("GuitarChordChartLog:: The notes are: " + NotesEnum.getString(chordNotes[0]) + ", "
                + NotesEnum.getString(chordNotes[1]) + ", "
                + NotesEnum.getString(chordNotes[2]) + ", "
                + NotesEnum.getString(chordNotes[3]));

        for (int i = 0; i <= numberOfFrets; i++) {
            for (int ii = 0; ii < numberOfStrings; ii++) {
                if (chordTab[ii] == -1 && (NotesEnum.fromInteger((guitarTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[0]
                        || NotesEnum.fromInteger((guitarTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[1]
                        || NotesEnum.fromInteger((guitarTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[2]
                        || NotesEnum.fromInteger((guitarTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[3])) {
                    chordTab[ii] = i;
                }
            }
        }
        Log.l("GuitarChordChartLog:: The tabs are: " + chordTab[0]
                + ", " + chordTab[1]
                + ", " + chordTab[2]
                + ", " + chordTab[3]
                + ", " + chordTab[4]
                + ", " + chordTab[5]);
        return chordTab;
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
                chordNotes[3] = NotesEnum.fromInteger((tonic.getValue() + 11) % NotesEnum.numberOfNotes);   //The seventh
                break;
            case Diminished5th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 6) % NotesEnum.numberOfNotes);    //The diminished fifth
                break;
            case Augmented5th:
                chordNotes[1] = NotesEnum.fromInteger((tonic.getValue() + 8) % NotesEnum.numberOfNotes);    //The augmented fifth
                break;
            case Other:
            default:
                break;
        }
        return chordNotes;
    }

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] guitarChordChart = GuitarChordChart.getChordTab(tonic, chordType);
        ImageView guitarChordStringView;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 1; i <= numberOfStrings; i++) {
            int guitarChordStringViewId = ctx.getResources().getIdentifier("guitar_chord_string_0" + i, "id", ctx.getPackageName());
            guitarChordStringView = ((Activity)ctx).findViewById(guitarChordStringViewId);
            guitarChordStringView.setAlpha(alpha);
            guitarChordStringView.setVisibility(View.VISIBLE);
            int guitarChordStringImageId;
            if (guitarChordChart[numberOfStrings - i] == -1) {
                guitarChordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0", "drawable", ctx.getPackageName());
            } else {
                guitarChordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0" + guitarChordChart[numberOfStrings - i], "drawable", ctx.getPackageName());
            }
            guitarChordStringView.setImageResource(guitarChordStringImageId);
        }
        ImageView fretView = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_frets);
        fretView.setVisibility(View.VISIBLE);
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }
}
