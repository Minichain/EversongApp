#include "processAudio.h"
#include <vector>

ProcessAudio::ProcessAudio(int sampleRate, int frameSize, int numHarmonics, int numOctaves, int numBinsToSearch) {
    c = Chromagram(frameSize, sampleRate, frameSize, numHarmonics, numOctaves, numBinsToSearch);
    c.setInputAudioFrameSize(frameSize);
    c.setSamplingFrequency(sampleRate);
    c.setChromaCalculationInterval(frameSize);

    Yin_init(&yin, (int16_t)frameSize, 0.30);
}

int* ProcessAudio::chordDetection(double* samples, double* spectrumSamples, int chordDetectionAlgorithm) {
    c.setMagnitudeSpectrum(spectrumSamples);
    c.setChordDetectionAlgorithm(chordDetectionAlgorithm);
    c.processAudioFrame(NULL);

    if (c.isReady()) {
        std::vector<double> chroma = c.getChromagram();
        chordDetector.detectChord(chroma);
        chordDetectionOutput[0] = chordDetector.rootNote;
        chordDetectionOutput[1] = chordDetector.chordType;
    }
    return chordDetectionOutput;
}

double ProcessAudio::getChordProbability() {
    return chordDetector.chordProbability;
}

double* ProcessAudio::getChromagram() {
    std::vector<double> chroma(12, -1.0);
    chroma = c.getChromagram();
    for (int i = 0; i < 12; i++) {
        chromagram[i] = chroma.at(i);
    }
    return chromagram;
}

float ProcessAudio::getPitch(double* samples, int length) {
    int16_t* samplesInt16 = new int16_t[length];
    for(int i = 0; i < length; i++) {
        samplesInt16[i] = (int16_t)(samples[i] * SHRT_MAX);
    }
    float pitchDetected = Yin_getPitch(&yin, samplesInt16);
    LOGI("ProcessAudioLog:: Pitch detected: %f", pitchDetected);
    delete []samplesInt16;
    return pitchDetected;
}

float ProcessAudio::getPitchProbability() {
    return Yin_getProbability(&yin);
}

double ProcessAudio::getAverageLevel(double* samples, int length) {
    double sumOfSamples = 0.0;
    for (int i = 0; i < length; i++) {
        sumOfSamples += samples[i];
    }
    return (sumOfSamples / (double)length);
}

double* ProcessAudio::bandPassFilter(double* samples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize) {
    int lowCutOffFreqIndex = (int)((lowCutOffFreq / (float)sampleRate) * frameSize) * 2;
    int highCutOffFreqIndex = (int)((highCutOffFreq / (float)sampleRate) * frameSize) * 2;
    samples = removeZeroFrequency(samples);
    float attenuationFactor = 1.2f;
    for (int i = 0; i < frameSize; i++) {
        if (lowCutOffFreqIndex > i){
            samples[i] = samples[i] / (attenuationFactor * (lowCutOffFreqIndex - i));
        } else if (lowCutOffFreqIndex <= i && i <= highCutOffFreqIndex) {
            samples[i] = samples[i];
        } else if (i > highCutOffFreqIndex) {
            samples[i] = samples[i] / (attenuationFactor * (i - highCutOffFreqIndex));
        }
    }
    return samples;
}

double* ProcessAudio::removeZeroFrequency(double* samples) {
    samples[0] = 0;
    samples[1] = 0;
    samples[2] = 0;
    return samples;
}

/**
 * The following code (FFT function) is an adaptation from a code written in Java by:
 * @author Orlando Selenu
 */

double* ProcessAudio::fft(double* inputReal, int length, bool DIRECT) {
    return fft(inputReal, NULL, length, DIRECT);
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
    // it's here to readability).
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

double* ProcessAudio::window(double* samples, int length, int windowType) {
    switch(windowType) {
        case 0:
            return samples;
        case 1:
            return hanning(samples, length);
        case 2:
            return hamming(samples, length);
        case 3:
        default:
            return blackman(samples, length);
    }
}

double* ProcessAudio::hanning(double* samples, int vectorLength) {
    for(int i = 0; i < vectorLength; i++) {
        samples[i] = (0.5 * (1.0 - cos(2.0*M_PI*(double)i/(double)(vectorLength - 1)))) * samples[i];
    }
    return samples;
}

double* ProcessAudio::hamming(double* samples, int vectorLength) {
    for(int i = 0; i < vectorLength; i++) {
        samples[i] = (0.54 - 0.46 * cos(2.0 * M_PI * (double)i/(double)(vectorLength - 1))) * samples[i];
    }
    return samples;
}

double* ProcessAudio::blackman(double* samples, int vectorLength) {
    for(int i = 0; i < vectorLength; i++) {
        samples[i] = (0.42 - 0.5 * cos(2.0*M_PI*(double)i/(double)(vectorLength-1)) + 0.08 * cos (4.0*M_PI*(double)i/(double)(vectorLength-1))) * samples[i];
    }
    return samples;
}

double ProcessAudio::getSpectralFlatness(double* spectrumSamples, int length) {
    double sumVal = 0.0;
    double logSumVal = 0.0;
    double N = (double)length;

    double flatness;

    for (int i = 0; i < length; i++) {
        // add one to stop zero values making it always zero
        double v = 1 + spectrumSamples[i];

        sumVal += v;
        logSumVal += log (v);
    }

    sumVal = sumVal / N;
    logSumVal = logSumVal / N;

    if (sumVal > 0) {
        flatness = exp(logSumVal) / sumVal;
    } else {
        flatness = 0.0;
    }

    return flatness;
}