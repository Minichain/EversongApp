package com.upf.minichain.eversongapp.chordChart;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.upf.minichain.eversongapp.Log;
import com.upf.minichain.eversongapp.R;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public class StaffChordChart extends ChordChart {
    private static int numberOfLines = 5;   //All staffs have 5 lines
    private static int numberOfNotes = 11;
    private static Clef clef = Clef.G_CLEF;

    enum Clef {
        G_CLEF, C_CLEF, F_CLEF
    }

    public static void setChordChart(Context ctx, NotesEnum tonic, ChordTypeEnum chordType, float alpha) {
        int[] chordChart = getStaffChordTab(tonic, chordType);
        int staffNoteViewId;
        ConstraintLayout staffNoteView;
        int staffSharpViewId;
        ImageView staffSharpView;
        int notesPainted = 1;
        final int initialPadding = 21;
        final int paddingBetweenNotes = 11;
//        for (int i = 0; i < numberOfNotes; i++) {
//            Log.l("StaffChordChartLog:: chordChart[" + i + "]: " + chordChart[i]);
//        }

        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.staff_chord_chart_layout);
        chordChartLayout.setVisibility(View.VISIBLE);

        for (int i = 0; (i < numberOfNotes) && (notesPainted <= 4); i++) {
            if (chordChart[i] != 0) {
                staffNoteViewId = ctx.getResources().getIdentifier("staff_chord_chart_note_layout_" + notesPainted, "id", ctx.getPackageName());
                staffNoteView = ((Activity)ctx).findViewById(staffNoteViewId);
                staffNoteView.setVisibility(View.VISIBLE);

                staffSharpViewId = ctx.getResources().getIdentifier("staff_chord_chart_sharp_" + notesPainted, "id", ctx.getPackageName());
                staffSharpView = ((Activity)ctx).findViewById(staffSharpViewId);
                if (chordChart[i] == 1) {           //Not sharp note
                    staffSharpView.setVisibility(View.GONE);
                } else if (chordChart[i] == 2) {    //Sharp note
                    staffSharpView.setVisibility(View.VISIBLE);
                }

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) staffNoteView.getLayoutParams();

                if (i > 0 && chordChart[i - 1] != 0) {  //To avoid notes overlap we set a margin on the left
                    params.setMargins((int)Utils.convertDpToPixel((float)(40), ctx),
                            0,
                            0,
                            (int)Utils.convertDpToPixel((float)(initialPadding + i * paddingBetweenNotes), ctx));
                } else {
                    params.setMargins(0,
                            0,
                            0,
                            (int)Utils.convertDpToPixel((float)(initialPadding + i * paddingBetweenNotes), ctx));
                }
                staffNoteView.setLayoutParams(params);
                notesPainted++;
            }
        }
        for (int i = notesPainted; i <= 4; i++) {
            staffNoteViewId = ctx.getResources().getIdentifier("staff_chord_chart_note_layout_" + i, "id", ctx.getPackageName());
            staffNoteView = ((Activity)ctx).findViewById(staffNoteViewId);
            staffNoteView.setVisibility(View.GONE);

            staffSharpViewId = ctx.getResources().getIdentifier("staff_chord_chart_sharp_" + i, "id", ctx.getPackageName());
            staffSharpView = ((Activity)ctx).findViewById(staffSharpViewId);
            staffSharpView.setVisibility(View.GONE);
        }
    }

    private static int[] getStaffChordTab(NotesEnum tonic, ChordTypeEnum chordType) {
        int[] chordTab = new int[numberOfNotes];
        for (int i = 0; i < numberOfNotes; i++) {
            chordTab[i] = 0;
        }
        NotesEnum[] chordNotes = getChordNotes(tonic, chordType);
//        Log.l("StaffChordChartLog:: chordNotes: " + chordNotes[0]
//                + ", " + chordNotes[1]
//                + ", " + chordNotes[2]
//                + ", " + chordNotes[3]);
        NotesEnum firstStaffChordChartNote = getFirstChordChartNote(clef);

        int noteLoop = firstStaffChordChartNote.getValue();
        boolean isTriadChord = chordNotes[3] == NotesEnum.NO_NOTE;
        int notesToSet = isTriadChord ? 3 : 4;
        boolean tonicSet = false;

        for (int i = 0; i < numberOfNotes; i ++) {
            if (!tonicSet) {
                if (NotesEnum.getString(tonic).contains(NotesEnum.getString(NotesEnum.fromInteger(noteLoop)))) {
                    if (NotesEnum.getString(tonic).contains("#")) {
                        chordTab[i] = 2;
                    } else {
                        chordTab[i] = 1;
                    }
                    tonicSet = true;
                }
            } else {
                for (int ii = 1; ii < notesToSet; ii++) {
                    if (NotesEnum.getString(chordNotes[ii]).contains(NotesEnum.getString(NotesEnum.fromInteger(noteLoop)))) {
                        if (NotesEnum.getString(chordNotes[ii]).contains("#")) {
                            chordTab[i] = 2;
                        } else {
                            chordTab[i] = 1;
                        }
                    }
                }
            }
            if (NotesEnum.fromInteger(noteLoop) == NotesEnum.B || NotesEnum.fromInteger(noteLoop) == NotesEnum.E) {
                noteLoop = (noteLoop + 1) % NotesEnum.numberOfNotes;
            } else {
                noteLoop = (noteLoop + 2) % NotesEnum.numberOfNotes;
            }
        }
        return chordTab;
    }

    /**
     * Depending on the "clef" the first note of the staff changes.
     * We consider the first note of the staff as the note right before
     * the first line starting from the bottom (Example: D in G clef).
     * */
    private static NotesEnum getFirstChordChartNote(Clef clef) {
        switch(clef) {
            case C_CLEF:
                return NotesEnum.E;
            case F_CLEF:
                return NotesEnum.F;
            case G_CLEF:
            default:
                return NotesEnum.D;
        }
    }

    public static void hideChordChart(Context ctx) {
        LinearLayout chordChartLayout = ((Activity)ctx).findViewById(R.id.staff_chord_chart_layout);
        chordChartLayout.setVisibility(View.GONE);
    }
}
