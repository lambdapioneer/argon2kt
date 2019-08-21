// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

#include <jni.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_lambdapioneer_argon2kt_Argon2JniVerification_verifyJniByAddingOne(JNIEnv *env, jobject thiz, jint input) {
    return input + 1;
}
