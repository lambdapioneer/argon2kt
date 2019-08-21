// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

#include <argon2.h>
#include <jni.h>
#include <malloc.h>
#include <cstring>

enum Argon2Jni_ErrorCodes {
    ARGON2JNI_OK = 0,
    ARGON2JNI_PASSWORD_BYTEBUFFER_NULL = 1000,
    ARGON2JNI_SALT_BYTEBUFFER_NULL = 1001,
    ARGON2JNI_ENCODED_BYTEBUFFER_NULL = 1002,
    ARGON2JNI_MALLOC_FAILED = 1003,
};

extern "C" JNIEXPORT jint JNICALL Java_com_lambdapioneer_argon2kt_Argon2Jni_nativeArgon2Hash(
        JNIEnv *env,
        jobject /* thiz */,
        jint mode,
        jint version,
        jint t_cost,
        jint m_cost,
        jint parallelism,
        jobject password,
        jobject salt,
        jint hash_length,
        jobject hash_destination,
        jobject encoded_destination) {
    // extract password from bytebuffer
    const jlong password_length = env->GetDirectBufferCapacity(password);
    const void *password_bytes = env->GetDirectBufferAddress(password);
    if (!password_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_PASSWORD_BYTEBUFFER_NULL;

    // extract salt from bytebuffer
    const jlong salt_length = env->GetDirectBufferCapacity(salt);
    const void *salt_bytes = env->GetDirectBufferAddress(salt);
    if (!salt_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_SALT_BYTEBUFFER_NULL;

    // allocate direct buffer for hash output
    void *hash_bytes = malloc(static_cast<size_t>(hash_length));
    if (!hash_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_MALLOC_FAILED;
    jobject hashDestinationBuffer = env->NewDirectByteBuffer(hash_bytes, hash_length);

    // allocate direct buffer for encoded output
    const size_t encoded_length = argon2_encodedlen(
            static_cast<const uint32_t>(t_cost),
            static_cast<const uint32_t>(m_cost),
            static_cast<const uint32_t>(parallelism),
            static_cast<const uint32_t>(salt_length),
            static_cast<const uint32_t>(hash_length),
            static_cast<argon2_type>(mode));

    char *encoded_bytes = static_cast<char *>(malloc(encoded_length));
    if (!encoded_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_MALLOC_FAILED;
    jobject encodedDestinationBuffer = env->NewDirectByteBuffer(encoded_bytes, encoded_length);

    // run argon2
    const int argon2_return_code = argon2_hash(
            static_cast<const uint32_t>(t_cost),
            static_cast<const uint32_t>(m_cost),
            static_cast<const uint32_t>(parallelism),
            password_bytes, static_cast<const size_t>(password_length),
            salt_bytes, static_cast<const uint32_t>(salt_length),
            hash_bytes, static_cast<const uint32_t>(hash_length),
            encoded_bytes, encoded_length,
            static_cast<argon2_type>(mode),
            static_cast<const uint32_t>(version));

    if (argon2_return_code != ARGON2_OK) {
        return argon2_return_code;
    }

    // static fields allow caching which makes sub-sequent access faster
    static jclass byteBufferTargetClazz = env->FindClass("com/lambdapioneer/argon2kt/ByteBufferTarget");
    static jfieldID byteBufferTargetField = env->GetFieldID(
            byteBufferTargetClazz,
            "byteBuffer",
            "Ljava/nio/ByteBuffer;"
    );

    // set byte buffer outputs to respective targets
    env->SetObjectField(hash_destination, byteBufferTargetField, hashDestinationBuffer);
    env->SetObjectField(encoded_destination, byteBufferTargetField, encodedDestinationBuffer);

    return ARGON2JNI_OK;
}

extern "C" JNIEXPORT jint JNICALL Java_com_lambdapioneer_argon2kt_Argon2Jni_nativeArgon2Verify(
        JNIEnv *env,
        jobject /* thiz */,
        jint mode,
        jobject encoded,
        jobject password) {
    // extract encoded from bytebuffer
    const jlong password_length = env->GetDirectBufferCapacity(password);
    const void *password_bytes = env->GetDirectBufferAddress(password);
    if (!password_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_PASSWORD_BYTEBUFFER_NULL;

    // extract password from bytebuffer (must be \0 terminated)
    const void *encoded_bytes = env->GetDirectBufferAddress(encoded);
    if (!encoded_bytes) return Argon2Jni_ErrorCodes::ARGON2JNI_ENCODED_BYTEBUFFER_NULL;

    return argon2_verify(
            static_cast<const char *>(encoded_bytes),
            password_bytes,
            static_cast<const size_t>(password_length),
            static_cast<argon2_type>(mode));
}
