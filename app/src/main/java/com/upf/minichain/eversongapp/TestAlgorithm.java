package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;

import java.util.ArrayList;

/**
 * This class is used to test the algorithm's performance with an audio sample with some known chords playing on it.
 * These chords are initiated in the method "initTestingChordsList" arrayList and compared with the chords detected
 * and stored in the "arrayOfChordsDetected" arrayList.
 * */
public class TestAlgorithm {

    public static double computeAlgorithmPerformance(ArrayList<String> chordsDetected) {
        double performance = 0;
        if (chordsDetected.isEmpty()) {
            return performance;
        }

        ArrayList<String> testingChords = initTestingChordsList();

        int testingChordChecking = 0;
        int chordsChecked = 0;
        int chordsRight = 0;

        for (int i = 0; i < chordsDetected.size(); i++) {
            while(parseTimeFromChordElement(testingChords.get(testingChordChecking + 1)) < parseTimeFromChordElement(chordsDetected.get(i))) {
                testingChordChecking++;
            }
            String testingChord = parseChordFromChordElement(testingChords.get(testingChordChecking));
            String detectedChord = parseChordFromChordElement(chordsDetected.get(i));

            Log.l("TestingAlgorithmLog:: testingChord: " + testingChord + ", detectedChord: " + detectedChord);
            if (testingChord.equals(detectedChord)) {
                chordsRight++;
            }
            chordsChecked++;
        }
        performance = ((double)chordsRight / (double)chordsChecked);
        return performance;
    }

    private static ArrayList<String> initTestingChordsList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("0 ms: " + NotesEnum.E.toString() + " " + ChordTypeEnum.Major.toString());
        list.add("4000 ms: " + NotesEnum.E.toString() + " " + ChordTypeEnum.Major7th.toString());
        list.add("8000 ms: " + NotesEnum.E.toString() + " " + ChordTypeEnum.Dominant7th.toString());
        list.add("12000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Major.toString());
        list.add("16000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Major7th.toString());
        list.add("20000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Dominant7th.toString());
        list.add("24000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Minor7th.toString());
        list.add("28000 ms: " + NotesEnum.C.toString() + " " + ChordTypeEnum.Augmented5th.toString());
        list.add("32000 ms: " + NotesEnum.D.toString() + " " + ChordTypeEnum.Minor.toString());
        list.add("36000 ms: " + NotesEnum.G.toString() + " " + ChordTypeEnum.Major.toString());
        list.add("40000 ms: " + NotesEnum.D.toString() + " " + ChordTypeEnum.Major.toString());
        list.add("44000 ms: " + NotesEnum.D.toString() + " " + ChordTypeEnum.Dominant7th.toString());
        list.add("48000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Diminished5th.toString());
        list.add("52000 ms: " + NotesEnum.F.toString() + " " + ChordTypeEnum.Sus2.toString());
        list.add("56000 ms: " + NotesEnum.D.toString() + " " + ChordTypeEnum.Sus2.toString());
        list.add("60000 ms: " + NotesEnum.A.toString() + " " + ChordTypeEnum.Sus2.toString());
        list.add("64000 ms: " + NotesEnum.NO_NOTE.toString() + " " + ChordTypeEnum.NoChord.toString());
        return list;
    }

    public static long parseTimeFromChordElement(String element) {
        long millisecondsParsed = -1;
        String subString;
        subString = element.substring(0, element.indexOf(" ms:"));
        millisecondsParsed = Integer.parseInt(subString);
        return millisecondsParsed;
    }

    public static String parseChordFromChordElement(String element) {
        String chord;
        chord = element.substring(element.indexOf(" ms: ") + (" ms: ").length(), element.length());
        return chord;
    }
}
