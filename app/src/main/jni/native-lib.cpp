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
        jintArray output = env->NewIntArray(2);
        env->SetIntArrayRegion(output, 0, 2, processAudio->chordDetection(env->GetDoubleArrayElements(samples, 0), env->GetDoubleArrayElements(spectrumSamples, 0)));
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_getChromagramJni(JNIEnv *env, jobject, jdoubleArray samples, jdoubleArray spectrumSamples) {
        jdoubleArray output = env->NewDoubleArray(12);
        env->SetDoubleArrayRegion(output, 0, 12, processAudio->getChromagram(env->GetDoubleArrayElements(samples, 0), env->GetDoubleArrayElements(spectrumSamples, 0)));
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_fftJni(JNIEnv *env, jobject, jdoubleArray inputReal, jboolean DIRECT) {
        jsize length = env->GetArrayLength(inputReal);
        jdoubleArray output = NULL;
        output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, ProcessAudio::fft(env->GetDoubleArrayElements(inputReal, 0), length, DIRECT));
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_windowJni(JNIEnv *env, jobject, jdoubleArray samples, jint windowType) {
        jsize length = env->GetArrayLength(samples);
        jdoubleArray output = NULL;
        output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, ProcessAudio::window(env->GetDoubleArrayElements(samples, 0), length, windowType));
        return output;
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_bandPassFilterJni(JNIEnv *env, jobject,
                                                                                 jdoubleArray spectrumSamples,
                                                                                 jfloat lowCutOffFreq,
                                                                                 jfloat highCutOffFreq,
                                                                                 jint sampleRate,
                                                                                 jint frameSize) {
        jsize length = env->GetArrayLength(spectrumSamples);
        jdoubleArray output = NULL;
        output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, ProcessAudio::bandPassFilter(env->GetDoubleArrayElements(spectrumSamples, 0), lowCutOffFreq, highCutOffFreq, sampleRate, frameSize));
        return output;
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
}