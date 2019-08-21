// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

class Argon2Exception(errorMessage: String) : RuntimeException(errorMessage) {
    companion object {
        fun fromErrorCode(errorCode: Int) = Argon2Exception("${Argon2Error.fromErrorCode(errorCode)} ($errorCode)")
    }
}

@Suppress("unused")
enum class Argon2Error constructor(val errorCode: Int) {
    ARGON2_OK(0),

    ARGON2_OUTPUT_PTR_NULL(-1),
    ARGON2_OUTPUT_TOO_SHORT(-2),
    ARGON2_OUTPUT_TOO_LONG(-3),
    ARGON2_PWD_TOO_SHORT(-4),
    ARGON2_PWD_TOO_LONG(-5),
    ARGON2_SALT_TOO_SHORT(-6),
    ARGON2_SALT_TOO_LONG(-7),
    ARGON2_AD_TOO_SHORT(-8),
    ARGON2_AD_TOO_LONG(-9),
    ARGON2_SECRET_TOO_SHORT(-10),
    ARGON2_SECRET_TOO_LONG(-11),
    ARGON2_TIME_TOO_SMALL(-12),
    ARGON2_TIME_TOO_LARGE(-13),
    ARGON2_MEMORY_TOO_LITTLE(-14),
    ARGON2_MEMORY_TOO_MUCH(-15),
    ARGON2_LANES_TOO_FEW(-16),
    ARGON2_LANES_TOO_MANY(-17),
    ARGON2_PWD_PTR_MISMATCH(-18),
    ARGON2_SALT_PTR_MISMATCH(-19),
    ARGON2_SECRET_PTR_MISMATCH(-20),
    ARGON2_AD_PTR_MISMATCH(-21),
    ARGON2_MEMORY_ALLOCATION_ERROR(-22),
    ARGON2_FREE_MEMORY_CBK_NULL(-23),
    ARGON2_ALLOCATE_MEMORY_CBK_NULL(-24),
    ARGON2_INCORRECT_PARAMETER(-25),
    ARGON2_INCORRECT_TYPE(-26),
    ARGON2_OUT_PTR_MISMATCH(-27),
    ARGON2_THREADS_TOO_FEW(-28),
    ARGON2_THREADS_TOO_MANY(-29),
    ARGON2_MISSING_ARGS(-30),
    ARGON2_ENCODING_FAIL(-31),
    ARGON2_DECODING_FAIL(-32),
    ARGON2_THREAD_FAIL(-33),
    ARGON2_DECODING_LENGTH_FAIL(-34),
    ARGON2_VERIFY_MISMATCH(-35),

    ARGON2JNI_PASSWORD_BYTEBUFFER_NULL(1000),
    ARGON2JNI_SALT_BYTEBUFFER_NULL(1001),
    ARGON2JNI_ENCODED_BYTEBUFFER_NULL(1002),
    ARGON2JNI_MALLOC_FAILED(1003);

    companion object {

        fun fromErrorCode(errorCode: Int): Argon2Error {
            val values = values()
            for (argonError in values) {
                if (errorCode == argonError.errorCode) {
                    return argonError
                }
            }
            throw IllegalArgumentException("Unknown error code: $errorCode")
        }
    }
}
