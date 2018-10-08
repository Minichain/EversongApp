#include <android/log.h>
#include "ProcessAudio.h"

double ProcessAudio::getAverageLevel(double samples[], int length) {
    double sumOfSamples = 0.0;
    for (int i = 0; i < length; i++) {
        sumOfSamples += samples[i];
    }
    return (sumOfSamples / (double)length);
}
