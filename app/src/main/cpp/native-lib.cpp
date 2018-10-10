#include <jni.h>
#include <string>
#include "ProcessAudio.h"

extern "C" {

    // Method used for TESTING purposes
    jstring Java_com_upf_minichain_eversongapp_AudioUtils_helloWorldTestCpp(JNIEnv *env, jobject) {
        std::string helloWorld = "Hello World!";
        return env->NewStringUTF(helloWorld.c_str());
    }

    jdoubleArray Java_com_upf_minichain_eversongapp_AudioUtils_fftCpp(JNIEnv *env, jobject, jdoubleArray inputReal, jboolean DIRECT) {
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

    jdouble Java_com_upf_minichain_eversongapp_AudioUtils_getAverageLevelCpp(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        jdouble *doublePointer = env->GetDoubleArrayElements(samples, 0);
        double* samplesArrayTemp;
        samplesArrayTemp = doublePointer;
        return ProcessAudio::getAverageLevel(samplesArrayTemp, length);
    }
}