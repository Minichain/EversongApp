#include "processAudio.h"
#include <vector>

ProcessAudio::ProcessAudio(int sample_rate, int frame_size, int hop_size) {
    sampleRate = sample_rate;
    frameSize = frame_size;
    hopSize = hop_size;
}

void ProcessAudio::chordDetection() {
    LOGI("essentiaChordDetection sampleRate: %d", sampleRate);
    Chromagram c (frameSize,sampleRate);
}

double ProcessAudio::getAverageLevel(double* samples, int length) {
    double sumOfSamples = 0.0;
    for (int i = 0; i < length; i++) {
        sumOfSamples += samples[i];
    }
    return (sumOfSamples / (double)length);
}

/**
 * The following code (FFT function) is an adaptation from a code written in Java by:
 * @author Orlando Selenu
 */

double* ProcessAudio::fft(double* inputReal, int length, bool DIRECT) {
    return ProcessAudio::fft(inputReal, NULL, length, DIRECT);
}

double* ProcessAudio::fft(double* inputReal, double* inputImag, int length, bool DIRECT) {
    double* output = NULL;

    double ld = log(length) / log(2.0);

    if (((int) ld) - ld != 0) {
        // "The number of elements is not a power of 2."
        return output;
    }

    int nu = (int) ld;
    int n2 = length / 2;
    int nu1 = nu - 1;
    double xReal[length];
    double xImag[length];
    double tReal, tImag, p, arg, c, s;

    // Here I check if I'm going to do the direct transform or the inverse transform.
    double constant;
    if (DIRECT) {
        constant = -2 * M_PI;
    } else {
        constant = 2 * M_PI;
    }

    // I don't want to overwrite the input arrays, so here I copy them. This
    // choice adds \Theta(2n) to the complexity.
    if (inputImag == NULL) {
        for (int i = 0; i < length; i++) {
            xReal[i] = *(inputReal + i);
            xImag[i] = sin(acos(*(inputReal + i)));
        }
    } else {
        for (int i = 0; i < length; i++) {
            xReal[i] = *(inputReal + i);
            xImag[i] = *(inputImag + i);
        }
    }

    // First phase - calculation
    int k = 0;
    for (int l = 1; l <= nu; l++) {
        while (k < length) {
            for (int i = 1; i <= n2; i++) {
                p = ProcessAudio::bitReverseReference(k >> nu1, nu);
                // direct FFT or inverse FFT
                arg = constant * p / length;
                c = cos(arg);
                s = sin(arg);
                tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                xReal[k + n2] = xReal[k] - tReal;
                xImag[k + n2] = xImag[k] - tImag;
                xReal[k] += tReal;
                xImag[k] += tImag;
                k++;
            }
            k += n2;
        }
        k = 0;
        nu1--;
        n2 /= 2;
    }

    // Second phase - recombination
    k = 0;
    int r;
    while (k < length) {
        r = ProcessAudio::bitReverseReference(k, nu);
        if (r > k) {
            tReal = xReal[k];
            tImag = xImag[k];
            xReal[k] = xReal[r];
            xImag[k] = xImag[r];
            xReal[r] = tReal;
            xImag[r] = tImag;
        }
        k++;
    }

    // Here I have to mix xReal and xImag to have an array (yes, it should
    // be possible to do this stuff in the earlier parts of the code, but
    // it's here to readibility).
    int newArrayLength = (int)sizeof(xReal) * 2;
    output = new double[newArrayLength];
    double radice = 1.0 / sqrt((double)length);
    for (int i = 0; i < newArrayLength; i += 2) {
        int i2 = (int)((double)i / 2.0);
        // I used Stephen Wolfram's Mathematica as a reference so I'm going
        // to normalize the output while I'm copying the elements.
        if (i2 < length) {
            output[i] = abs(xReal[i2] * radice);
            output[i + 1] = abs(xImag[i2] * radice);
        }
    }
    return output;
}

/**
 * The reference bitreverse function.
 */
int ProcessAudio::bitReverseReference(int j, int nu) {
    int j2;
    int j1 = j;
    int k = 0;
    for (int i = 1; i <= nu; i++) {
        j2 = j1 / 2;
        k = 2 * k + j1 - 2 * j2;
        j1 = j2;
    }
    return k;
}