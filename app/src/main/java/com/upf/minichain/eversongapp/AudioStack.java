package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class AudioStack {
    static {
        System.loadLibrary("native-lib");
    }

    public static void initAudioStack() {
        initProcessAudioJni(Parameters.SAMPLE_RATE, Parameters.BUFFER_SIZE, Parameters.HOP_SIZE);
    }

    public static int[] chordDetection(double[] samples, double[] spectrumSamples, int chordDetectionAlgorithm) {
        return chordDetectionJni(samples, spectrumSamples, chordDetectionAlgorithm);
    }

    public static double[] getChromagram() {
        return getChromagramJni();
    }

    public static float getPitch(double[] samples) {
        return getPitchJni(samples);
    }

    public static float getPitchProbability() {
        return getPitchProbabilityJni();
    }

    public static double[] window(double[] samples, WindowFunctionEnum windowType) {
        return windowJni(samples, windowType.getValue());
    }

    public static double[] fft(double[] inputReal, boolean DIRECT) {
        return fftJni(inputReal, DIRECT);
    }

    public static double[] highPassFilter(double[] samples, float cutOffFreq) {
        //TODO
        return samples;
    }

    public static double[] lowPassFilter(double[] samples, float cutOffFreq) {
        //TODO
        return samples;
    }

    public static double[] bandPassFilter(double[] samples, float lowCutOffFreq, float highCutOffFreq) {
        return bandPassFilterJni(samples, lowCutOffFreq, highCutOffFreq, Parameters.SAMPLE_RATE, Parameters.BUFFER_SIZE);
    }

    public static double getAverageLevel(double[] samples) {
        return getAverageLevelJni(samples);
    }

    public static double getSpectralFlatness(double[] spectralSamples) {
        return getSpectralFlatnessJni(spectralSamples);
    }

    public static double getDifference(double[] buffer1, double[] buffer2) {
        return Math.abs(getAverageLevelJni(buffer1) - getAverageLevelJni(buffer2));
    }

    public static NotesEnum getNoteByFrequency(double frequencyValue) {
        double frequencyDistance = 1000.0;
        NotesEnum noteDetected = NotesEnum.NO_NOTE;
        double tempFrequency = 0.0;
        int i = 0;
        float maxFreqToCheck = 10000f;
        while (tempFrequency < maxFreqToCheck) {
            tempFrequency = Math.pow(NotesEnum.refValue, ((double)i - 49.0)) * NotesEnum.refFreq;
            double newFreqDistance = Math.abs(tempFrequency - frequencyValue);
            if (newFreqDistance < frequencyDistance) {
                frequencyDistance = newFreqDistance;
                noteDetected = NotesEnum.fromInteger((i - 1) % NotesEnum.numberOfNotes);
            }
            i++;
        }
        return noteDetected;
    }

    public static float getFrequencyPeak(double[] spectrum, double threshold) {
        int valueIndex = getMaxSpectrumValueIndex(spectrum, threshold);
        if (valueIndex != -1) {
            return getFrequencyByIndex(valueIndex);
        } else {
            return -1;
        }
    }

    public static int getMaxSpectrumValueIndex(double[] buffer, double threshold) {
        int maxValueIndex = -1;
        double maxValue = 0.0;
        for (int i = 0; i < (buffer.length / 2); i++) {
            if (buffer[i] >= threshold && buffer[i] > maxValue) {
                maxValueIndex = i;
                maxValue = buffer[i];
            }
        }
        return maxValueIndex;
    }

    public static float getFrequencyByIndex(int index) {
        return (float)Parameters.SAMPLE_RATE * ((float)index / Parameters.BUFFER_SIZE);
    }

    public static int getIndexByFrequency(float freq) {
        return (int)((freq * Parameters.BUFFER_SIZE * 2) / Parameters.SAMPLE_RATE);
    }

    public static double[] getSamplesToDouble(short[] inputBuffer) {
        double[] outputBuffer = new double[inputBuffer.length];
        for (int i = 0; i < inputBuffer.length;  i++) {
            outputBuffer[i] = (double)inputBuffer[i] / (double)Short.MAX_VALUE;
        }
        return outputBuffer;
    }

    /**
     * This function returns the most probable chord within the list of
     * chords stored in the ChordBuffer with size = CHORD_BUFFER_SIZE
     * */
    public static int[] getMostProbableChord(int[][] chordsDetectedBuffer) {
        int[] mostProbableChord = new int[3];
        mostProbableChord[0] = -1;  //Root note
        mostProbableChord[1] = -1;  //Chord type
        mostProbableChord[2] = -1;  //Probability

        String[] listOfChords = new String[Parameters.getInstance().getChordBufferSize()];
        int[] chordsProbability = new int[Parameters.getInstance().getChordBufferSize()];
        int numOfDifferentChords = 0;

        for (int i = 0; i < Parameters.getInstance().getChordBufferSize(); i++) {
            String chordName = NotesEnum.fromInteger(chordsDetectedBuffer[i][0]).toString()
                    + ChordTypeEnum.fromInteger(chordsDetectedBuffer[i][1]).toString();

            for (int j = 0; j < Parameters.getInstance().getChordBufferSize(); j++) {
                if (listOfChords[j] == null || listOfChords[j].equals("")) {
                    listOfChords[j] = chordName;
                    chordsProbability[j]++;
                    numOfDifferentChords++;
                    break;
                } else if (listOfChords[j].equals(chordName)) {
                    chordsProbability[j]++;
                    break;
                }
                numOfDifferentChords++;
            }
        }

        String mostProbableChordString = listOfChords[getMaxValueAndIndex(chordsProbability)[1]];

        mostProbableChord[0] = getChordFromString(mostProbableChordString)[0];
        mostProbableChord[1] = getChordFromString(mostProbableChordString)[1];
        mostProbableChord[2] = (int)(100f * (float)getMaxValueAndIndex(chordsProbability)[0] / (float)numOfDifferentChords);        //Percentage

        return mostProbableChord;
    }

    /**
     * This method takes a chord in String format and returns it in int[] format,
     * where index 0 is the rootNote (A) and index 1 is the chordType (Major)
     * */
    public static int[] getChordFromString(String chordString) {
        int[] chord = new int[2];
        chord[1] = 0;   //NoChord as default ChordType
        for (int i = 0; i < NotesEnum.numberOfNotes; i++) {
            if (chordString.startsWith(NotesEnum.fromInteger(i).toString()) && chordString.contains("#")) {
                chord[0] = i + 1;
                break;
            } else if (chordString.startsWith(NotesEnum.fromInteger(i).toString())) {
                chord[0] = i;
                break;
            }
        }
        for (int i = 0; i < ChordTypeEnum.numberOfChordTypes; i++) {
            if (chordString.endsWith(ChordTypeEnum.fromInteger(i).toString())) {
                chord[1] = i;
            }
        }
        return chord;
    }

    /**
     * This method takes a buffer of (int) samples and returns
     * the max value within the samples and its position.
     * */
    public static int[] getMaxValueAndIndex(int[] buffer) {
        int[] returnValue = new int[2];
        returnValue[0] = -1;     //value
        returnValue[1] = -1;    //index
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > returnValue[0]) {
                returnValue[0] = buffer[i];
                returnValue[1] = i;
            }
        }
        return returnValue;
    }

    /**
     * These methods are used in order
     * to access to the C++ implementation (native-lib.cpp)
     **/
    private static native void initProcessAudioJni(int sample_rate, int frame_size, int hop_size);

    private static native int[] chordDetectionJni(double[] samples, double[] spectrumSamples, int chordDetectionAlgorithm);

    private static native double[] getChromagramJni();

    private static native double[] windowJni(double[] samples, int windowType);

    private static native double[] fftJni(double[] inputReal, boolean DIRECT);

    private static native double[] bandPassFilterJni(double[] spectrumSamples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);

    private static native double getAverageLevelJni(double[] samples);

    private static native double getSpectralFlatnessJni(double[] samples);

    private static native float getPitchJni(double[] samples);

    private static native float getPitchProbabilityJni();
}
