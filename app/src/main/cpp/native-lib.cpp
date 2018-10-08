#include <jni.h>
#include <string>
#include "ProcessAudio.h"

extern "C" {
    ProcessAudio processAudio;

    // Method used for TESTING
    jstring Java_com_upf_minichain_eversongapp_MainActivity_helloWorldTest(JNIEnv *env, jobject) {
        std::string helloWorld = "Hello World!";
        return env->NewStringUTF(helloWorld.c_str());
    }

    jdouble Java_com_upf_minichain_eversongapp_MainActivity_getAverageLevel(JNIEnv *env, jobject, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        jdouble *doublePointer = env->GetDoubleArrayElements(samples, 0);
        double samplesArrayTemp[length];
        for (int i = 0; i < length; i++) {
            samplesArrayTemp[i] = doublePointer[i];
        }
        return processAudio.getAverageLevel(samplesArrayTemp, length);
    }
}