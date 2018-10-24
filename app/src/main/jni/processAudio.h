#ifndef EVERSONGAPP_PROCESSAUDIO_H
#define EVERSONGAPP_PROCESSAUDIO_H

#include <math.h>
#include <android/log.h>
#include <iostream>
#include "essentia/include/essentia/essentia.h"
#include "essentia/include/essentia/algorithmfactory.h"
#include "essentia/include/essentia/essentiamath.h"
#include "essentia/include/essentia/pool.h"

#define  LOG_TAG    "EversongAppLog"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

class ProcessAudio {
public:
    ProcessAudio* processAudio;
    int sampleRate;
    int frameSize;
    int hopSize;

    ProcessAudio(int sample_rate, int frame_size, int hop_size);

    static double getAverageLevel(double samples[], int length);
    static double* fft(double* inputReal, int length, bool DIRECT);
    static double* fft(double* inputReal, double* inputImag, int length, bool DIRECT);
    static int bitReverseReference(int j, int nu);
};

#endif //EVERSONGAPP_PROCESSAUDIO_H