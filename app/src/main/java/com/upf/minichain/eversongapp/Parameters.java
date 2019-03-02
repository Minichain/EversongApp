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
    private static final Parameters instance = new Parameters();
    private static SQLiteDatabase parametersDatabase;

    public static Parameters getInstance() {
        return instance;
    }

    /*************
     * PARAMETERS
     ************/
    public static int SAMPLE_RATE;          // The sampling rate (16000, 22050, 44100)
    public static int BUFFER_SIZE;          // It must be a power of 2 (2048, 4096, 8192, 16384...)
    public static int HOP_SIZE;             // It must be a power of 2
    public static int FRAMES_PER_SECOND;
    public static int BANDPASS_FILTER_LOW_FREQ;
    public static int BANDPASS_FILTER_HIGH_FREQ;

    private static MusicalNotationEnum musicalNotation;
    private static WindowFunctionEnum windowingFunction;
    private static ChartTab chartTab;
    private static ChordDetectionAlgorithm chordDetectionAlgorithm;
    private static EversongFunctionalities functionalityTab;
    private static int chordBufferSize;
    private static int pitchBufferSize;
    private static boolean debugMode;

    /******************
     * INIT PARAMETERS
     *****************/
    private Parameters() {
        SAMPLE_RATE = 44100;
        BUFFER_SIZE = 8192;
        HOP_SIZE = 2048;
        FRAMES_PER_SECOND = 10;
        BANDPASS_FILTER_LOW_FREQ = 55;
        BANDPASS_FILTER_HIGH_FREQ = 4000;

        musicalNotation = MusicalNotationEnum.ENGLISH_NOTATION;
        windowingFunction = WindowFunctionEnum.HAMMING_WINDOW;
        chordBufferSize = 7;
        pitchBufferSize = 4;
        chartTab = ChartTab.GUITAR_TAB;
        functionalityTab = EversongFunctionalities.CHORD_DETECTION;
        chordDetectionAlgorithm = ChordDetectionAlgorithm.ADAM_STARK_ALGORITHM;
        debugMode = false;
    }

    public void loadParameters(Context context) {
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

            tempValue = loadParameter("debugMode");
            debugMode = tempValue == 1;
        }
    }

    public int loadParameter(String parameter) {
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

//    public void setDatabaseParameters(Context context) {
//        ParametersDatabaseHelper parametersDbHelper = new ParametersDatabaseHelper(context);
//        parametersDatabase = parametersDbHelper.getWritableDatabase();
//
//        setParameterInDataBase("SAMPLE_RATE", SAMPLE_RATE);
//        setParameterInDataBase("BUFFER_SIZE", BUFFER_SIZE);
//        setParameterInDataBase("HOP_SIZE", HOP_SIZE);
//        setParameterInDataBase("FRAMES_PER_SECOND", FRAMES_PER_SECOND);
//    }

    public void setParameterInDataBase(String parameter, int value) {
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

    public boolean isParameterInDataBase(String parameter) {
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

    public void setAudioBufferSize(int newSize) {
        setParameterInDataBase("BUFFER_SIZE", newSize);
        BUFFER_SIZE = newSize;
    }

    private int checkAudioBufferValue(int tempValue) {
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

    public void setBandpassFilterLowFreq(int bandpassFilterLowFreq) {
        setParameterInDataBase("BANDPASS_FILTER_LOW_FREQ", bandpassFilterLowFreq);
        BANDPASS_FILTER_LOW_FREQ = bandpassFilterLowFreq;
    }

    public void setBandpassFilterHighFreq(int bandpassFilterHighFreq) {
        setParameterInDataBase("BANDPASS_FILTER_HIGH_FREQ", bandpassFilterHighFreq);
        BANDPASS_FILTER_HIGH_FREQ = bandpassFilterHighFreq;
    }

    public void setMusicalNotation(MusicalNotationEnum notation) {
        setParameterInDataBase("musicalNotation", notation.getValue());
        musicalNotation = notation;
    }

    public MusicalNotationEnum getMusicalNotation() {
        return musicalNotation;
    }

    public void setWindowingFunction(WindowFunctionEnum window) {
        setParameterInDataBase("windowingFunction", window.getValue());
        windowingFunction = window;
    }

    public WindowFunctionEnum getWindowingFunction() {
        return windowingFunction;
    }

    public void setChordBufferSize(int newSize) {
        setParameterInDataBase("chordBufferSize", newSize);
        chordBufferSize = newSize;
    }

    public int getChordBufferSize() {
        return chordBufferSize;
    }

    public int getPitchBufferSize() {
        return pitchBufferSize;
    }

    public void setFunctionalitySelected(EversongFunctionalities tab) {
        setParameterInDataBase("functionalityTab", tab.getValue());
        functionalityTab = tab;
    }

    public EversongFunctionalities getFunctionalitySelected() {
        return functionalityTab;
    }

    public void setChartTabSelected(ChartTab tab) {
        setParameterInDataBase("chartTab", tab.getValue());
        chartTab = tab;
    }

    public ChartTab getChartTabSelected() {
        return chartTab;
    }

    public void setChordDetectionAlgorithm(ChordDetectionAlgorithm algorithm) {
        setParameterInDataBase("chordDetectionAlgorithm", algorithm.getValue());
        chordDetectionAlgorithm = algorithm;
    }

    public ChordDetectionAlgorithm getChordDetectionAlgorithm() {
        return chordDetectionAlgorithm;
    }

    public void setDebugMode(boolean value) {
        setParameterInDataBase("debugMode", value ? 1 : 0);
        debugMode = value;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
