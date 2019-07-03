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
 *      for the chord "A minor" it would be {0, 1, 2, 2, 0, 0}
 *      for the chord "C major" it would be {0, 1, 0, 2, 3, 0}
 *      for the chord "F major" it would be {1, 1, 2, 3, 3, 1}
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
        int[] chordTab = null;

        if (!isStandardTuning(guitarTuning) || numberOfStrings != 6) {
            return chordTab;
        }

        switch(tonic) {
            case A:
                break;
            case A_SHARP:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {1, 3, 3, 3, 1, -1};
                        break;
                    case Minor:
                        chordTab = new int[] {1, 2, 3, 3, 1, -1};
                        break;
                    case Sus2:
                        chordTab = new int[] {1, 1, 3, 3, 1, -1};
                        break;
                    case Sus4:
                        chordTab = new int[] {1, 4, 3, 3, 1, -1};
                        break;
                    case Dominant7th:
                        chordTab = new int[] {1, 3, 1, 3, 1, -1};
                        break;
                    case Major7th:
                        chordTab = new int[] {1, 3, 2, 3, 1, -1};
                        break;
                    case Minor7th:
                        chordTab = new int[] {1, 2, 1, 3, 1, -1};
                        break;
                    case Augmented5th:
                        chordTab = new int[] {2, 3, 3, 4, -1, -1};
                        break;
                    case Power5th:
                        chordTab = new int[] {-1, -1, 3, 3, 1, -1};
                        break;
                }
                break;
            case B:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {2, 4, 4, 4, 2, -1};
                        break;
                    case Minor:
                        chordTab = new int[] {2, 3, 4, 4, 2, -1};
                        break;
                    case Sus2:
                        chordTab = new int[] {2, 2, 4, 4, 2, -1};
                        break;
                    case Sus4:
                        chordTab = new int[] {2, 5, 4, 4, 2, -1};
                        break;
                    case Dominant7th:
                        chordTab = new int[] {2, 4, 2, 4, 2, -1};
                        break;
                    case Major7th:
                        chordTab = new int[] {2, 4, 3, 4, 2, -1};
                        break;
                    case Minor7th:
                        chordTab = new int[] {2, 3, 2, 4, 2, -1};
                        break;
                    case Augmented5th:
                        chordTab = new int[] {3, 4, 4, 5, -1, -1};
                        break;
                    case Power5th:
                        chordTab = new int[] {-1, -1, 4, 4, 2, -1};
                        break;
                }
                break;
            case C:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {0, 1, 0, 2, 3, 0};
                        break;
                    case Minor:
                        chordTab = new int[] {3, 4, 5, 5, 3, -1};
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
                        chordTab = new int[] {0, 0, 1, 2, 2, 0};
                        break;
                    case Minor:
                        chordTab = new int[] {0, 0, 0, 2, 2, 0};
                        break;
                }
                break;
            case F:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {1, 1, 2, 3, 3, 1};
                        break;
                    case Minor:
                        chordTab = new int[] {1, 1, 1, 3, 3, 1};
                        break;
                }
                break;
            case F_SHARP:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {2, 2, 3, 4, 4, 2};
                        break;
                    case Minor:
                        chordTab = new int[] {2, 2, 2, 4, 4, 2};
                        break;
                }
                break;
            case G:
                switch(chordType) {
                    case Major:
                        chordTab = new int[] {3, 0, 0, 0, 2, 3};
                        break;
                    case Minor:
                        chordTab = new int[] {3, 3, 3, 5, 5, 3};
                        break;
                }
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

    private static boolean isStandardTuning(NotesEnum[] guitarTuning) {
        if (guitarTuning[0] == NotesEnum.E
                && guitarTuning[1] == NotesEnum.B
                && guitarTuning[2] == NotesEnum.G
                && guitarTuning[3] == NotesEnum.D
                && guitarTuning[4] == NotesEnum.A
                && guitarTuning[5] == NotesEnum.E) {
            return true;
        } else {
            return false;
        }
    }
}
