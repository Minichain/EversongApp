//
// Created by Minichain on 07/10/2018.
//

#include <jni.h>
#include <string>

extern "C" {
    jstring
    Java_com_upf_minichain_eversongapp_MainActivity_stringFromJNI(
            JNIEnv *env,
            jobject ) {
        std::string helloWorld = "Hello World!";
        return env->NewStringUTF(helloWorld.c_str());
    }
}