package com.upf.minichain.eversongapp.chordChart;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.upf.minichain.eversongapp.R;
import com.upf.minichain.eversongapp.Utils;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class PianoChordChart {
    private static int numberOfKeys = 12;
    private static NotesEnum firstKeyNote = NotesEnum.C;

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] chordChart = getPianoChordTab(tonic, chordType);
        ImageView chordKeyView;
        int chordKeyViewId;
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.piano_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 1; i <= numberOfKeys; i++) {
            if (i <= 9) {
                chordKeyViewId = ctx.getResources().getIdentifier("piano_key_0" + i, "id", ctx.getPackageName());
            } else {
                chordKeyViewId = ctx.getResources().getIdentifier("piano_key_" + i, "id", ctx.getPackageName());
            }
            chordKeyView = ((Activity)ctx).findViewById(chordKeyViewId);
            chordKeyView.setAlpha(alpha);
            chordKeyView.setVisibility(View.VISIBLE);
            if (chordChart[i - 1] == -1) {
                chordKeyView.setColorFilter(ctx.getResources().getColor(R.color.colorPrimary));
            } else {
                chordKeyView.setColorFilter(ctx.getResources().getColor(R.color.mColor01));
            }
        }
    }

    public static int[] getPianoChordTab(NotesEnum tonic, ChordTypeEnum chordType) {
        NotesEnum[] chordNotes = Utils.getChordNotes(tonic, chordType);
        int[] chordTab = new int[numberOfKeys];
        for (int i = 0; i < numberOfKeys; i++) {
            chordTab[i] = -1;
        }

        for (int i = 0; i < numberOfKeys; i++) {
            if (chordTab[i] == -1 && (NotesEnum.fromInteger((firstKeyNote.getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[0]
                    || NotesEnum.fromInteger((firstKeyNote.getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[1]
                    || NotesEnum.fromInteger((firstKeyNote.getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[2]
                    || NotesEnum.fromInteger((firstKeyNote.getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[3])) {
                chordTab[i] = 1;
            }
        }

        return chordTab;
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.piano_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }
}
