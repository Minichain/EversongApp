#ifndef EVERSONGAPP_PROCESSAUDIO_H
#define EVERSONGAPP_PROCESSAUDIO_H

#include <math.h>
#include <android/log.h>
#include <iostream>

#define  LOG_TAG    "EversongAppLog"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#include "ChordDetection/Chromagram.h"
#include "ChordDetection/ChordDetector.h"
extern "C" {
    #include "PitchTracker/Yin.h"
};

class ProcessAudio {
public:
    ProcessAudio* processAudio;
    int sampleRate;
    int frameSize;
    Chromagram c = Chromagram(0, 0, 0, 0, 0, 0);
    double* chromagram = new double[SEMITONES];
    ChordDetector chordDetector;
    int* chordDetectionOutput = new int[2];
    Yin yin;

    ProcessAudio(int sampleRate, int frameSize, int numHarmonics, int numOctaves, int numBinsToSearch);
    int* chordDetection(double* samples, double* spectrumSamples, int chordDetectionAlgorithm);
    double getChordProbability();
    double* getChromagram();
    float getPitch(double* samples, int length);
    float getPitchProbability();

    static double* bandPassFilter(double* samples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);
    static double* removeZeroFrequency(double* samples);

    static double* window(double* samples, int length, int windowType);
    static double* fft(double* inputReal, int length, bool DIRECT);
    static double* fft(double* inputReal, double* inputImag, int length, bool DIRECT);
    static int bitReverseReference(int j, int nu);

    /**Audio Features**/
    static double getAverageLevel(double samples[], int length);
    static double getSpectralFlatness(double* inputSamples, int vectorLength);

private:
    static double* hanning(double* inputSamples, int vectorLength);
    static double* hamming(double* inputSamples, int vectorLength);
    static double* blackman(double* inputSamples, int vectorLength);
};

#endif //EVERSONGAPP_PROCESSAUDIO_H