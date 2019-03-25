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
 * This class is designed to retrieve the guitar tabs of a certain chord.
 * For example...
 *      for the chord "A minor" it would be {0, 0, 2, 2, 1, 0}
 *      for the chord "C major" it would be {0, 3, 2, 0, 1, 0}
 *      for the chord "F major" it would be {1, 3, 3, 2, 1, 1}
 */
public class GuitarChordChart extends ChordChart {
    private static int numberOfStrings = 6;
    private static int numberOfFrets = 4;
    private static NotesEnum[] guitarTuning = new NotesEnum[numberOfStrings];

    static {
        guitarTuning[0] = NotesEnum.E;
        guitarTuning[1] = NotesEnum.B;
        guitarTuning[2] = NotesEnum.G;
        guitarTuning[3] = NotesEnum.D;
        guitarTuning[4] = NotesEnum.A;
        guitarTuning[5] = NotesEnum.E;
    }

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] chordChart;
        chordChart = checkKnownChords(tonic, chordType, numberOfFrets, numberOfStrings, guitarTuning);
        if (chordChart == null) {
            chordChart = getChordTab(tonic, chordType, numberOfFrets, numberOfStrings, guitarTuning);
        }
        ImageView guitarChordStringView;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 1; i <= numberOfStrings; i++) {
            int guitarChordStringViewId = ctx.getResources().getIdentifier("guitar_chord_string_0" + i, "id", ctx.getPackageName());
            guitarChordStringView = ((Activity)ctx).findViewById(guitarChordStringViewId);
            guitarChordStringView.setAlpha(alpha);
            guitarChordStringView.setVisibility(View.VISIBLE);
            int guitarChordStringImageId;
            if (chordChart[i - 1] == -1) {
                guitarChordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0", "drawable", ctx.getPackageName());
            } else {
                guitarChordStringImageId = ctx.getResources().getIdentifier("guitar_chord_string_0" + chordChart[i - 1], "drawable", ctx.getPackageName());
            }
            guitarChordStringView.setImageResource(guitarChordStringImageId);
        }
        ImageView fretView = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_frets);
        fretView.setVisibility(View.VISIBLE);
    }

    /**
     * Known chords for guitar in standard tuning
     * */
    private static int[] checkKnownChords(NotesEnum tonic, ChordTypeEnum chordType, int numberOfFrets, int numberOfStrings, NotesEnum[] guitarTuning) {
        int[] chordTab = new int[numberOfStrings];

        switch(tonic) {
            case B:
                switch(chordType) {
                    case Major:
                        chordTab[0] = 2;
                        chordTab[1] = 4;
                        chordTab[2] = 4;
                        chordTab[3] = 4;
                        chordTab[4] = 2;
                        chordTab[5] = -1;
                        return chordTab;
                    case Minor:
                        chordTab[0] = 2;
                        chordTab[1] = 3;
                        chordTab[2] = 4;
                        chordTab[3] = 4;
                        chordTab[4] = 2;
                        chordTab[5] = -1;
                        return chordTab;
                }
            case C:
                switch(chordType) {
                    case Major:
                        chordTab[0] = 0;
                        chordTab[1] = 1;
                        chordTab[2] = 0;
                        chordTab[3] = 2;
                        chordTab[4] = 3;
                        chordTab[5] = 0;
                        return chordTab;
                    case Minor:
                        chordTab[0] = 3;
                        chordTab[1] = 4;
                        chordTab[2] = 5;
                        chordTab[3] = 5;
                        chordTab[4] = 3;
                        chordTab[5] = -1;
                        return chordTab;
                }
            case F:
                switch(chordType) {
                    case Major:
                        chordTab[0] = 1;
                        chordTab[1] = 1;
                        chordTab[2] = 2;
                        chordTab[3] = 3;
                        chordTab[4] = 3;
                        chordTab[5] = 1;
                        return chordTab;
                    case Minor:
                        chordTab[0] = 1;
                        chordTab[1] = 1;
                        chordTab[2] = 1;
                        chordTab[3] = 3;
                        chordTab[4] = 3;
                        chordTab[5] = 1;
                        return chordTab;
                }
            case G:
                switch(chordType) {
                    case Major:
                        chordTab[0] = 3;
                        chordTab[1] = 0;
                        chordTab[2] = 0;
                        chordTab[3] = 0;
                        chordTab[4] = 2;
                        chordTab[5] = 3;
                        return chordTab;
                    case Minor:
                        chordTab[0] = 3;
                        chordTab[1] = 3;
                        chordTab[2] = 3;
                        chordTab[3] = 5;
                        chordTab[4] = 5;
                        chordTab[5] = 3;
                        return chordTab;
                }
        }
        return null;
    }

    public static void setTuningChordChart(Context ctx, NotesEnum pitchNote, float pitchFreq) {
        ImageView stringView;
        int stringViewId;
        int stringImageId;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);
        for (int i = 1; i <= numberOfStrings; i++) {
            stringViewId = ctx.getResources().getIdentifier("guitar_chord_string_0" + i, "id", ctx.getPackageName());
            stringView = ((Activity)ctx).findViewById(stringViewId);
            stringImageId = ctx.getResources().getIdentifier("guitar_chord_string_00", "drawable", ctx.getPackageName());
            stringView.setImageResource(stringImageId);
            if (guitarTuning[i - 1] == pitchNote) {
                stringView.setAlpha(1f);
            } else {
                stringView.setAlpha(0.4f);
            }
        }
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.guitar_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }
}
