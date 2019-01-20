package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.Log;
import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

public abstract class ChordChart {

    public static int[] getChordTab(NotesEnum tonic, ChordTypeEnum chordType, int numberOfFrets, int numberOfStrings, NotesEnum[] tuning) {
        int[] chordTab = new int[numberOfStrings];
        for (int i = 0; i < numberOfStrings; i++) {
            chordTab[i] = -1;
        }

        NotesEnum[] chordNotes = getChordNotes(tonic, chordType);
        Log.l("ChordChartLog:: The chord is: " + NotesEnum.getString(tonic) + " " + chordType);

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
            case Other:
            default:
                break;
        }
        return chordNotes;
    }
}
