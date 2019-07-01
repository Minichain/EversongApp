package com.upf.minichain.eversongapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.upf.minichain.eversongapp.enums.ChartTab;
import com.upf.minichain.eversongapp.enums.ChordDetectionAlgorithm;
import com.upf.minichain.eversongapp.enums.EversongFunctionalities;
import com.upf.minichain.eversongapp.enums.MusicalNotationEnum;
import com.upf.minichain.eversongapp.enums.WindowFunctionEnum;

public class Parameters {
    private static SQLiteDatabase parametersDatabase;

    /*************
     * PARAMETERS
     ************/
    public static int SAMPLE_RATE = 44100;                  // The sampling rate (11025, 16000, 22050, 44100)
    public static int BUFFER_SIZE = 8192;                   // It must be a power of 2 (2048, 4096, 8192, 16384...)
    public static int HOP_SIZE = 2048;                      // It must be a power of 2
    public static int FRAMES_PER_SECOND = 10;
    public static int BANDPASS_FILTER_LOW_FREQ = 55;
    public static int BANDPASS_FILTER_HIGH_FREQ = 4000;

    private static MusicalNotationEnum musicalNotation = MusicalNotationEnum.ENGLISH_NOTATION;
    private static WindowFunctionEnum windowingFunction = WindowFunctionEnum.HANNING_WINDOW;
    private static ChartTab chartTab = ChartTab.GUITAR_TAB;
    private static ChordDetectionAlgorithm chordDetectionAlgorithm = ChordDetectionAlgorithm.ADAM_STARK_ALGORITHM;
    private static EversongFunctionalities functionalityTab = EversongFunctionalities.CHORD_DETECTION;
    private static int chordBufferSize = 7;
    private static int pitchBufferSize = 3;
    private static int chordProbabilityThreshold = 95;
    private static boolean debugMode = false;

    //Chromagram parameters
    private static int chromagramNumHarmonics = 3;
    private static int chromagramNumOctaves = 2;
    private static int chromagramNumBinsToSearch = 3;

    private Parameters() {
    }

    public static void loadParameters(Context context) {
        ParametersDatabaseHelper parametersDbHelper = new ParametersDatabaseHelper(context);
        parametersDatabase = parametersDbHelper.getWritableDatabase();

        Log.l("ParametersLog:: Number of rows: " + (int) DatabaseUtils.queryNumEntries(parametersDatabase, ParametersDatabaseHelper.ParametersColumns.PARAMETERS_TABLE_NAME));

        if (parametersDatabase != null) {
            int tempValue;
            tempValue = loadParameter("SAMPLE_RATE");
            SAMPLE_RATE = (tempValue != -1) ? tempValue : SAMPLE_RATE;

            tempValue = loadParameter("BUFFER_SIZE");
            BUFFER_SIZE = checkAudioBufferValue(tempValue);

            tempValue = loadParameter("HOP_SIZE");
            HOP_SIZE = (tempValue != -1) ? tempValue : HOP_SIZE;

            tempValue = loadParameter("FRAMES_PER_SECOND");
            FRAMES_PER_SECOND = (tempValue != -1) ? tempValue : FRAMES_PER_SECOND;

            tempValue = loadParameter("BANDPASS_FILTER_LOW_FREQ");
            BANDPASS_FILTER_LOW_FREQ = (tempValue != -1) ? tempValue : BANDPASS_FILTER_LOW_FREQ;

            tempValue = loadParameter("BANDPASS_FILTER_HIGH_FREQ");
            BANDPASS_FILTER_HIGH_FREQ = (tempValue != -1) ? tempValue : BANDPASS_FILTER_HIGH_FREQ;

            tempValue = loadParameter("musicalNotation");
            musicalNotation = (tempValue != -1) ? MusicalNotationEnum.values()[tempValue] : musicalNotation;

            tempValue = loadParameter("windowingFunction");
            windowingFunction = (tempValue != -1) ? WindowFunctionEnum.values()[tempValue] : windowingFunction;

            tempValue = loadParameter("chordBufferSize");
            chordBufferSize = (tempValue != -1) ? tempValue : chordBufferSize;

            tempValue = loadParameter("functionalityTab");
            functionalityTab = (tempValue != -1) ? EversongFunctionalities.values()[tempValue] : functionalityTab;

            tempValue = loadParameter("chartTab");
            chartTab = (tempValue != -1) ? ChartTab.values()[tempValue] : chartTab;

            tempValue = loadParameter("chordDetectionAlgorithm");
            chordDetectionAlgorithm = (tempValue != -1) ? chordDetectionAlgorithm.values()[tempValue] : chordDetectionAlgorithm;

            tempValue = loadParameter("pitchBufferSize");
            pitchBufferSize = (tempValue != -1) ? tempValue : pitchBufferSize;

            tempValue = loadParameter("debugMode");
            debugMode = tempValue == 1;

            tempValue = loadParameter("chromagramNumHarmonics");
            chromagramNumHarmonics = (tempValue != -1) ? tempValue : chromagramNumHarmonics;

            tempValue = loadParameter("chromagramNumBinsToSearch");
            chromagramNumBinsToSearch = (tempValue != -1) ? tempValue : chromagramNumBinsToSearch;

            tempValue = loadParameter("chromagramNumBinsToSearch");
            chromagramNumBinsToSearch = (tempValue != -1) ? tempValue : chromagramNumBinsToSearch;
        }
    }

    private static int loadParameter(String parameter) {
        int parameterValue = -1;
        Cursor cursor =  parametersDatabase.rawQuery( "SELECT Value FROM ParametersTable WHERE Parameter = '" + parameter + "'", null);
        cursor.moveToFirst();
        try {
            parameterValue = cursor.getInt(0);
        } catch (Exception e) {
            cursor.close();
            Log.l("ParametersLog:: Error loading " + parameter + " parameter");
        }
        Log.l("ParametersLog:: " + parameter + " loaded. Value: " + parameterValue);
        cursor.close();
        return parameterValue;
    }

    private static void setParameterInDataBase(String parameter, int value) {
        if (parametersDatabase != null) {
            ContentValues newContent = new ContentValues();
            newContent.put(ParametersDatabaseHelper.ParametersColumns.COLUMN_PARAMETER, parameter);
            newContent.put(ParametersDatabaseHelper.ParametersColumns.COLUMN_VALUE, value);

            if (!isParameterInDataBase(parameter)) {
                parametersDatabase.insert(ParametersDatabaseHelper.ParametersColumns.PARAMETERS_TABLE_NAME, null, newContent);
            } else {
                Log.l("ParametersLog:: Parameter " + parameter + " is already in the database. Updating it with value: " + value);
                parametersDatabase.update(ParametersDatabaseHelper.ParametersColumns.PARAMETERS_TABLE_NAME, newContent, "Parameter = '" + parameter + "'", null);
            }
        }
    }

    private static boolean isParameterInDataBase(String parameter) {
        try {
            Cursor cursor =  parametersDatabase.rawQuery( "SELECT COUNT(Parameter) FROM ParametersTable WHERE Parameter = '" + parameter + "'", null);
            cursor.moveToFirst();
            if (cursor.getInt(0) != 0) {
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void setAudioBufferSize(int newSize) {
        setParameterInDataBase("BUFFER_SIZE", newSize);
        BUFFER_SIZE = newSize;
    }

    private static int checkAudioBufferValue(int tempValue) {
        switch(tempValue) {
            case 2048:
            case 4096:
            case 8192:
            case 16384:
                return tempValue;
            default:
                return BUFFER_SIZE;
        }
    }

    public static void setBandpassFilterLowFreq(int bandpassFilterLowFreq) {
        setParameterInDataBase("BANDPASS_FILTER_LOW_FREQ", bandpassFilterLowFreq);
        BANDPASS_FILTER_LOW_FREQ = bandpassFilterLowFreq;
    }

    public static void setBandpassFilterHighFreq(int bandpassFilterHighFreq) {
        setParameterInDataBase("BANDPASS_FILTER_HIGH_FREQ", bandpassFilterHighFreq);
        BANDPASS_FILTER_HIGH_FREQ = bandpassFilterHighFreq;
    }

    public static void setMusicalNotation(MusicalNotationEnum notation) {
        setParameterInDataBase("musicalNotation", notation.getValue());
        musicalNotation = notation;
    }

    public static MusicalNotationEnum getMusicalNotation() {
        return musicalNotation;
    }

    public static void setWindowingFunction(WindowFunctionEnum window) {
        setParameterInDataBase("windowingFunction", window.getValue());
        windowingFunction = window;
    }

    public static WindowFunctionEnum getWindowingFunction() {
        return windowingFunction;
    }

    public static void setChordBufferSize(int newSize) {
        setParameterInDataBase("chordBufferSize", newSize);
        chordBufferSize = newSize;
    }

    public static int getChordBufferSize() {
        return chordBufferSize;
    }

    public static void setPitchBufferSize(int newSize) {
        setParameterInDataBase("pitchBufferSize", newSize);
        pitchBufferSize = newSize;
    }

    public static int getPitchBufferSize() {
        return pitchBufferSize;
    }

    public static void setFunctionalitySelected(EversongFunctionalities tab) {
        setParameterInDataBase("functionalityTab", tab.getValue());
        functionalityTab = tab;
    }

    public static EversongFunctionalities getFunctionalitySelected() {
        return functionalityTab;
    }

    public static void setChartTabSelected(ChartTab tab) {
        setParameterInDataBase("chartTab", tab.getValue());
        chartTab = tab;
    }

    public static ChartTab getChartTabSelected() {
        return chartTab;
    }

    public static void setChordDetectionAlgorithm(ChordDetectionAlgorithm algorithm) {
        setParameterInDataBase("chordDetectionAlgorithm", algorithm.getValue());
        chordDetectionAlgorithm = algorithm;
    }

    public static ChordDetectionAlgorithm getChordDetectionAlgorithm() {
        return chordDetectionAlgorithm;
    }

    public static int getChordProbabilityThreshold() {
        return chordProbabilityThreshold;
    }

    public static void setDebugMode(boolean value) {
        setParameterInDataBase("debugMode", value ? 1 : 0);
        debugMode = value;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setChromagramNumHarmonics(int numHarmonics) {
        setParameterInDataBase("chromagramNumHarmonics", numHarmonics);
        chromagramNumHarmonics = numHarmonics;
    }

    public static int getChromagramNumHarmonics() {
        return chromagramNumHarmonics;
    }

    public static void setChromagramNumOctaves(int numHarmonics) {
        setParameterInDataBase("chromagramNumOctaves", numHarmonics);
        chromagramNumOctaves = numHarmonics;
    }

    public static int getChromagramNumOctaves() {
        return chromagramNumOctaves;
    }

    public static void setChromagramNumBinsToSearch(int numHarmonics) {
        setParameterInDataBase("chromagramNumBinsToSearch", numHarmonics);
        chromagramNumBinsToSearch = numHarmonics;
    }

    public static int getChromagramNumBinsToSearch() {
        return chromagramNumBinsToSearch;
    }
}
