#ifndef EVERSONGAPP_PROCESSAUDIO_H
#define EVERSONGAPP_PROCESSAUDIO_H

#include <math.h>

class ProcessAudio {
public:
    static double getAverageLevel(double samples[], int length);
    static double* fft(double* inputReal, int length, bool DIRECT);
    static double* fft(double* inputReal, double* inputImag, int length, bool DIRECT);
    static int bitReverseReference(int j, int nu);
};


#endif //EVERSONGAPP_PROCESSAUDIO_H
