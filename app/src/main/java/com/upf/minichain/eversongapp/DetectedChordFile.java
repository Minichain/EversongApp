package com.upf.minichain.eversongapp;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DetectedChordFile {
    FileOutputStream chordsDetectedOutputStream;
    OutputStreamWriter chordsDetectedOutputStreamWriter;
    BufferedWriter chordsDetectedBufferedWriter;
    long startTime;

    DetectedChordFile(Context context) {
        String filename = "chords_detected.txt";
//        File file = new File(context.getFilesDir(), filename);
        File file = new File(context.getExternalFilesDir(null), filename);
        Log.l("DetectedChordFileLog:: Creating file. Name: " + filename + ", Directory: " + context.getFilesDir());
        try {
            chordsDetectedOutputStream = new FileOutputStream(file);
            chordsDetectedOutputStreamWriter = new OutputStreamWriter(chordsDetectedOutputStream);
            chordsDetectedBufferedWriter = new BufferedWriter(chordsDetectedOutputStreamWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeInFile(String newElement) {
        try {
            chordsDetectedBufferedWriter.write(newElement + "\n");
            Log.l("DetectedChordFileLog:: writing to file. Content: \"" + newElement + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            Log.l("DetectedChordFileLog:: Closing file");
            chordsDetectedBufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
