#include <jni.h>
#include "processAudio.h"

extern "C" {
    /**
     * ProcessAudio functions
     */
    ProcessAudio* processAudio;

    void Java_com_upf_minichain_eversongapp_AudioStack_initProcessAudioJni(JNIEnv *env, jobject, jint sample_rate, jint frame_size, jint hop_size) {
        processAudio = new ProcessAudio(sample_rate, frame_size);
    }

    jintArray Java_com_upf_minichain_eversongapp_AudioStack_chordDetectionJni(JNIEnv *env, jobject, jdoubleArray samples, jdoubleArray spectrumSamples) {
        double* samplesArrayTemp = env->GetDoubleArrayElements(samples, 0);
        double* spectrumSamplesArrayTemp = env->GetDoubleArrayElements(spectrumSamples, 0);

        int* returnArray = processAudio->chordDetection(samplesArrayTemp, spectrumSamplesArrayTemp);
        jintArray output = env->NewIntArray(2);
        env->SetIntArrayRegion(output, 0, 2, returnArray);
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_fftJni(JNIEnv *env, jobject, jdoubleArray inputReal, jboolean DIRECT, jint windowType) {
        jsize length = env->GetArrayLength(inputReal);
        double* samplesArrayTemp = env->GetDoubleArrayElements(inputReal, 0);

        double* returnArray = ProcessAudio::fft(samplesArrayTemp, length, DIRECT, windowType);
        jdoubleArray output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, returnArray);
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_bandPassFilterJni(JNIEnv *env, jobject,
                                                                                 jdoubleArray spectrumSamples,
                                                                                 jfloat lowCutOffFreq,
                                                                                 jfloat highCutOffFreq,
                                                                                 jint sampleRate,
                                                                                 jint frameSize) {
        jsize length = env->GetArrayLength(spectrumSamples);
        double* spectrumSamplesArrayTemp = env->GetDoubleArrayElements(spectrumSamples, 0);

        double* returnArray = ProcessAudio::bandPassFilter(spectrumSamplesArrayTemp, lowCutOffFreq, highCutOffFreq, sampleRate, frameSize);
        jdoubleArray output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, returnArray);
        return output;
    }

    jdouble Java_com_upf_minichain_eversongapp_AudioStack_getAverageLevelJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        double* samplesArrayTemp = env->GetDoubleArrayElements(samples, 0);
        return ProcessAudio::getAverageLevel(samplesArrayTemp, length);
    }

    jfloat Java_com_upf_minichain_eversongapp_AudioStack_getPitchJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        double* samplesArrayTemp = env->GetDoubleArrayElements(samples, 0);
        return processAudio->getPitch(samplesArrayTemp, length);
    }
}