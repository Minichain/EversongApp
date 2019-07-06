package com.upf.minichain.eversongapp.chordChart;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.upf.minichain.eversongapp.R;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

/**
 * This class is designed to retrieve the ukulele tabs of a certain chord.
 * For example...
 *      for the chord "G major" it would be {2, 3, 2, 0}
 *      for the chord "C major" it would be {3, 0, 0, 0}
 */
//TODO include all chordChart classes into one big chordChart class
public class UkuleleChordChart extends ChordChart {
    public static int numberOfStrings = 4;
    public static int numberOfFrets = 4;
    public static NotesEnum[] ukuleleTuning = new NotesEnum[numberOfStrings];

    static {
        ukuleleTuning[0] = NotesEnum.A;
        ukuleleTuning[1] = NotesEnum.E;
        ukuleleTuning[2] = NotesEnum.C;
        ukuleleTuning[3] = NotesEnum.G;
    }

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] chordChart;
        chordChart = checkKnownChords(tonic, chordType, numberOfFrets, numberOfStrings, ukuleleTuning);
        if (chordChart == null) {
            chordChart = getChordTab(tonic, chordType, numberOfFrets, numberOfStrings, ukuleleTuning);
        }
        ImageView chordStringView;
        int chordStringViewId;
        int chordStringImageId;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 1; i <= numberOfStrings; i++) {
            chordStringViewId = ctx.getResources().getIdentifier("ukulele_chord_string_0" + i, "id", ctx.getPackageName());
            chordStringView = ((Activity)ctx).findViewById(chordStringViewId);
            chordStringView.setAlpha(alpha);
            chordStringView.setVisibility(View.VISIBLE);
            if (chordChart[i - 1] == -1) {
                chordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0", "drawable", ctx.getPackageName());
            } else {
                chordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0" + chordChart[i - 1], "drawable", ctx.getPackageName());
            }
            chordStringView.setImageResource(chordStringImageId);
        }
        ImageView fretView = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_frets);
        fretView.setVisibility(View.VISIBLE);
    }

    /**
     * Known chords for guitar in standard tuning
     * */
    private static int[] checkKnownChords(NotesEnum tonic, ChordTypeEnum chordType, int numberOfFrets, int numberOfStrings, NotesEnum[] ukuleleTuning) {
        int[] chordTab = null;

        if (!isStandardTuning(ukuleleTuning) || numberOfStrings != 4) {
            return chordTab;
        }

        switch(tonic) {
            case A:
                break;
            case A_SHARP:
                break;
            case B:
                break;
            case C:
                switch(chordType) {
                    case Minor:
                        chordTab = new int[] {3, 3, 3, 0};
                        break;
                }
                break;
            case C_SHARP:
                break;
            case D:
                break;
            case D_SHARP:
                break;
            case E:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {2, 4, 4, 4};
                        break;
                    case Minor:
                        chordTab = new int[] {2, 3, 4, 0};
                        break;
                }
                break;
            case F:
                break;
            case F_SHARP:
                break;
            case G:
                break;
            case G_SHARP:
                break;
        }
        return chordTab;
    }


    public static void setTuningChordChart(Context ctx, NotesEnum pitchNote, float pitchFreq) {
        ImageView stringView;
        int stringViewId;
        int stringImageId;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);
        for (int i = 1; i <= numberOfStrings; i++) {
            stringViewId = ctx.getResources().getIdentifier("ukulele_chord_string_0" + i, "id", ctx.getPackageName());
            stringView = ((Activity)ctx).findViewById(stringViewId);
            stringImageId = ctx.getResources().getIdentifier("guitar_chord_string_00", "drawable", ctx.getPackageName());
            stringView.setImageResource(stringImageId);
            if (ukuleleTuning[i - 1] == pitchNote) {
                stringView.setAlpha(1f);
            } else {
                stringView.setAlpha(0.4f);
            }
        }
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.ukulele_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }

    private static boolean isStandardTuning(NotesEnum[] guitarTuning) {
        if (guitarTuning[0] == NotesEnum.A
                && guitarTuning[1] == NotesEnum.E
                && guitarTuning[2] == NotesEnum.C
                && guitarTuning[3] == NotesEnum.G) {
            return true;
        } else {
            return false;
        }
    }
}
