#include <jni.h>
#include <string>
#include "processAudio.h"

extern "C" {
    /**
     * ProcessAudio functions
     */
    ProcessAudio* processAudio;

    void Java_com_upf_minichain_eversongapp_AudioStack_initProcessAudioJni(JNIEnv *env, jobject, jint sample_rate, jint frame_size, jint hop_size) {
        processAudio = new ProcessAudio(sample_rate, frame_size, hop_size);
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioStack_fftJni(JNIEnv *env, jobject, jdoubleArray inputReal, jboolean DIRECT) {
        jsize length = env->GetArrayLength(inputReal);
        jdouble *doublePointer = env->GetDoubleArrayElements(inputReal, 0);
        double* samplesArrayTemp;
        samplesArrayTemp = doublePointer;

        double* returnArray;
        returnArray = ProcessAudio::fft(samplesArrayTemp, length, DIRECT);

        jdoubleArray output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, returnArray);
        return output;
    }

    jdouble Java_com_upf_minichain_eversongapp_AudioStack_getAverageLevelJni(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        jdouble *doublePointer = env->GetDoubleArrayElements(samples, 0);
        double* samplesArrayTemp;
        samplesArrayTemp = doublePointer;
        return ProcessAudio::getAverageLevel(samplesArrayTemp, length);
    }
}