# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library(# Specifies the name of the library.
            native-lib

            # Sets the library as a shared library.
            SHARED

            # Provides a relative path to your source file(s).
            src/main/jni/native-lib.cpp
            src/main/jni/processAudio.cpp
            )

# Specifies a path to native header files.
include_directories(../../../../src/main/jni/include/)

add_library(essentia
            SHARED
            IMPORTED )

set_target_properties( # Specifies the target library.
            essentia

            # Specifies the parameter you want to define.
            PROPERTIES IMPORTED_LOCATION

            # Provides the path to the library you want to import.
            ../../../../src/main/jniLibs/${ANDROID_ABI}/libessentia.so )

find_library( # Sets the name of the path variable.
            log-lib

            # Specifies the name of the NDK library that
            # you want CMake to locate.
            log )

# Links your native library against one or more other native libraries.
target_link_libraries( # Specifies the target library.
            native-lib
            essentia
            ${log-lib}
            )