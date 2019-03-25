package com.upf.minichain.eversongapp.chordChart;

import com.upf.minichain.eversongapp.Log;
import com.upf.minichain.eversongapp.Utils;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public abstract class ChordChart {
    /**
     * This method is used to get the generic tabs from a given chord
     * and some information from the instrument: Number of frets used to play the chord,
     * number of strings of the instrument and tuning.
     * */
    public static int[] getChordTab(NotesEnum tonic, ChordTypeEnum chordType, int numberOfFrets, int numberOfStrings, NotesEnum[] tuning) {
        int[] chordTab = new int[numberOfStrings];
        for (int i = 0; i < numberOfStrings; i++) {
            chordTab[i] = -1;
        }

        NotesEnum[] chordNotes = Utils.getChordNotes(tonic, chordType);
//        Log.l("ChordChartLog:: The chord is: " + NotesEnum.getString(tonic) + " " + chordType);

        for (int i = 0; i <= numberOfFrets; i++) {
            for (int ii = 0; ii < numberOfStrings; ii++) {
                if (chordTab[ii] == -1 && (NotesEnum.fromInteger((tuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[0]
                        || NotesEnum.fromInteger((tuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[1]
                        || NotesEnum.fromInteger((tuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[2]
                        || NotesEnum.fromInteger((tuning[ii].getValue() + i) % NotesEnum.numberOfNotes) == chordNotes[3])) {
                    chordTab[ii] = i;
                }
            }
        }

//        for (int i = 0; i < 4; i++) {
//            Log.l("ChordChartLog:: chordNotes[" + i + "]: " + NotesEnum.getString(chordNotes[i]));
//        }
//        for (int i = 0; i < numberOfStrings; i++) {
//            Log.l("ChordChartLog:: chordTab[" + i + "]: " + chordTab[i]);
//        }

        return chordTab;
    }
}
