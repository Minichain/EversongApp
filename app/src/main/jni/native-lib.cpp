#include <jni.h>
#include "processAudio.h"

extern "C" {
    /**
     * ProcessAudio functions
     */
    ProcessAudio* processAudio;
    jintArray outputIntArray;
    jdoubleArray outputDoubleArray;

    void Java_com_upf_minichain_eversongapp_AudioStack_initProcessAudioJni(JNIEnv *env, jobject, jint sample_rate, jint frame_size, jint hop_size) {
        processAudio = new ProcessAudio(sample_rate, frame_size);
    }

    jintArray Java_com_upf_minichain_eversongapp_AudioStack_chordDetectionJni(JNIEnv *env, jobject, jdoubleArray samples, jdoubleArray spectrumSamples, jint chordDetectionAlgorithm) {
        outputIntArray = env->NewIntArray(2);
        env->SetIntArrayRegion(outputIntArray, 0, 2, processAudio->chordDetection(env->GetDoubleArrayElements(samples, 0), env->GetDoubleArrayElements(spectrumSamples, 0), chordDetectionAlgorithm));
        return outputIntArray;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_getChromagramJni(JNIEnv *env, jobject) {
        outputDoubleArray = env->NewDoubleArray(SEMITONES);
        env->SetDoubleArrayRegion(outputDoubleArray, 0, SEMITONES, processAudio->getChromagram());
        return outputDoubleArray;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_fftJni(JNIEnv *env, jobject, jdoubleArray inputReal, jboolean DIRECT) {
        jsize length = env->GetArrayLength(inputReal);
        double* samplesDouble = ProcessAudio::fft(env->GetDoubleArrayElements(inputReal, 0), length, DIRECT);
        outputDoubleArray = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(outputDoubleArray, 0, length, samplesDouble);
        delete[] samplesDouble;
        return outputDoubleArray;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_windowJni(JNIEnv *env, jobject, jdoubleArray samples, jint windowType) {
        jsize length = env->GetArrayLength(samples);
        outputDoubleArray = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(outputDoubleArray, 0, length, ProcessAudio::window(env->GetDoubleArrayElements(samples, 0), length, windowType));
        return outputDoubleArray;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_bandPassFilterJni(JNIEnv *env, jobject,
                                                                                 jdoubleArray spectrumSamples,
                                                                                 jfloat lowCutOffFreq,
                                                                                 jfloat highCutOffFreq,
                                                                                 jint sampleRate,
                                                                                 jint frameSize) {
        jsize length = env->GetArrayLength(spectrumSamples);
        outputDoubleArray = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(outputDoubleArray, 0, length, ProcessAudio::bandPassFilter(env->GetDoubleArrayElements(spectrumSamples, 0), lowCutOffFreq, highCutOffFreq, sampleRate, frameSize));
        return outputDoubleArray;
    }

    jdouble Java_com_upf_minichain_eversongapp_AudioStack_getAverageLevelJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        return ProcessAudio::getAverageLevel(env->GetDoubleArrayElements(samples, 0), length);
    }

    jdouble Java_com_upf_minichain_eversongapp_AudioStack_getSpectralFlatnessJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        return ProcessAudio::getSpectralFlatness(env->GetDoubleArrayElements(samples, 0), length);
    }

    jfloat Java_com_upf_minichain_eversongapp_AudioStack_getPitchJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        return processAudio->getPitch(env->GetDoubleArrayElements(samples, 0), length);
    }

    jfloat Java_com_upf_minichain_eversongapp_AudioStack_getPitchProbabilityJni(JNIEnv *env, jobject) {
        return processAudio->getPitchProbability();
    }

    jdouble Java_com_upf_minichain_eversongapp_AudioStack_getChordProbabilityJni(JNIEnv *env, jobject) {
        return processAudio->getChordProbability();
    }
}