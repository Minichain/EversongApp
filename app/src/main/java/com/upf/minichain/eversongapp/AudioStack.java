package com.upf.minichain.eversongapp;

import com.upf.minichain.eversongapp.enums.ChordTypeEnum;
import com.upf.minichain.eversongapp.enums.NotesEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public final class AudioStack {

    static {
        System.loadLibrary("native-lib");
    }

    public static void initAudioStack() {
        initProcessAudioJni(Parameters.SAMPLE_RATE, Parameters.BUFFER_SIZE, Parameters.HOP_SIZE);
    }

    public static int[] chordDetection(double[] samples, double[] spectrumSamples) {
        return chordDetectionJni(samples, spectrumSamples);
    }

    public static double[] getChromagram(double[] samples, double[] spectrumSamples) {
        return getChromagramJni(samples, spectrumSamples);
    }

    public static float getPitch(double[] samples) {
        return getPitchJni(samples);
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
                noteDetected = NotesEnum.fromInteger(((i - 4)) % NotesEnum.numberOfNotes);
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
     * This function returns the most probable chord from the list of
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
            String chordName = NotesEnum.getString(NotesEnum.fromInteger(chordsDetectedBuffer[i][0]))
                    + ChordTypeEnum.getString(ChordTypeEnum.fromInteger(chordsDetectedBuffer[i][1]));

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

    public static int[] getChordFromString(String chordString) {
        int[] chord = new int[2];
        for (int i = 0; i < NotesEnum.numberOfNotes; i++) {
            if (chordString.startsWith(NotesEnum.getString(NotesEnum.fromInteger(i)))) {
                chord[0] = i;
                break;
            }
        }
        for (int i = 0; i < ChordTypeEnum.numberOfChordTypes; i++) {
            if (chordString.endsWith(ChordTypeEnum.getString(ChordTypeEnum.fromInteger(i)))) {
                chord[1] = i;
            }
        }
        return chord;
    }

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
     * This methods are used in order
     * to access to the C++ implementation
     **/
    private static native void initProcessAudioJni(int sample_rate, int frame_size, int hop_size);

    private static native int[] chordDetectionJni(double[] samples, double[] spectrumSamples);

    private static native double[] getChromagramJni(double[] samples, double[] spectrumSamples);

    private static native double[] windowJni(double[] samples, int windowType);

    private static native double[] fftJni(double[] inputReal, boolean DIRECT);

    private static native double[] bandPassFilterJni(double[] spectrumSamples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);

    private static native double getAverageLevelJni(double[] samples);

    private static native float getPitchJni(double[] samples);
}
