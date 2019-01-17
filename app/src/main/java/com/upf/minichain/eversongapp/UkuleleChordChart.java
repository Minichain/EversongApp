package com.upf.minichain.eversongapp;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

/**
 * This class is designed to retrieve the ukulele tabs of a certain chord.
 * For example...
 *      for the chord "G major" it would be {0, 2, 3, 2}
 *      for the chord "C major" it would be {0, 0, 0, 3}
 */
//TODO include all chordChart classes into one big chordChart class
public class UkuleleChordChart {
    private static int numberOfStrings = 4;
    private static int numberOfFrets = 4;
    private static NotesEnum[] ukuleleTuning = new NotesEnum[numberOfStrings];

    static {
        ukuleleTuning[0] = NotesEnum.G;
        ukuleleTuning[1] = NotesEnum.C;
        ukuleleTuning[2] = NotesEnum.E;
        ukuleleTuning[3] = NotesEnum.A;
    }

    public static int[] getChordTab(NotesEnum tonic, ChordTypeEnum chordType) {
        int[] chordTab = new int[numberOfStrings];
        chordTab[0] = -1;
        chordTab[1] = -1;
        chordTab[2] = -1;
        chordTab[3] = -1;

        NotesEnum[] chordNotes = GuitarChordChart.getChordNotes(tonic, chordType);
        Log.l("UkuleleChordChartLog:: The chord is: " + NotesEnum.getString(tonic) + " " + chordType);
        Log.l("UkuleleChordChartLog:: The notes are: " + NotesEnum.getString(chordNotes[0]) + ", "
                + NotesEnum.getString(chordNotes[1]) + ", "
                + NotesEnum.getString(chordNotes[2]) + ", "
                + NotesEnum.getString(chordNotes[3]));

        for (int i = 0; i <= numberOfFrets; i++) {
            for (int ii = 0; ii < numberOfStrings; ii++) {
                if (chordTab[ii] == -1 && (NotesEnum.fromInteger((ukuleleTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[0]
                        || NotesEnum.fromInteger((ukuleleTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[1]
                        || NotesEnum.fromInteger((ukuleleTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[2]
                        || NotesEnum.fromInteger((ukuleleTuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[3])) {
                    chordTab[ii] = i;
                }
            }
        }
        Log.l("UkuleleChordChartLog:: The tabs are: " + chordTab[0]
                + ", " + chordTab[1]
                + ", " + chordTab[2]
                + ", " + chordTab[3]);
        return chordTab;
    }

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] chordChart = UkuleleChordChart.getChordTab(tonic, chordType);
        ImageView chordStringView;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 1; i <= numberOfStrings; i++) {
            int chordStringViewId = ctx.getResources().getIdentifier("ukulele_chord_string_0" + i, "id", ctx.getPackageName());
            chordStringView = ((Activity)ctx).findViewById(chordStringViewId);
            chordStringView.setAlpha(alpha);
            chordStringView.setVisibility(View.VISIBLE);
            int chordStringImageId;
            if (chordChart[numberOfStrings - i] == -1) {
                chordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0", "drawable", ctx.getPackageName());
            } else {
                chordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0" + chordChart[numberOfStrings - i], "drawable", ctx.getPackageName());
            }
            chordStringView.setImageResource(chordStringImageId);
        }
        ImageView fretView = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_frets);
        fretView.setVisibility(View.VISIBLE);
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }
}
