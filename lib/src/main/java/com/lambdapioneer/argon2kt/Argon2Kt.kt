// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import java.nio.ByteBuffer

internal const val ARGON2KT_DEFAULT_T_COST = 1 // number of iterations
internal const val ARGON2KT_DEFAULT_M_COST = 65536 // resulting in 64 MiB memory cost
internal const val ARGON2KT_DEFAULT_PARALLELISM = 2 // running two threads in parallel
internal const val ARGON2KT_DEFAULT_HASH_LENGTH = 32 // resulting in 32 bytes = 128 bit output
internal val ARGON2KT_DEFAULT_VERSION = Argon2Version.V13 // default to newest version V13

enum class Argon2Mode(val identifier: Int) {
    ARGON2_D(0),
    ARGON2_I(1),
    ARGON2_ID(2)
}

enum class Argon2Version(val version: Int) {
    V10(0x10),
    V13(0x13)
}

class Argon2Kt(soLoader: SoLoaderShim = SystemSoLoader()) {
    private val jni = Argon2Jni(soLoader)

    @JvmOverloads
    fun hash(
        mode: Argon2Mode,
        password: ByteArray,
        salt: ByteArray,
        tCostInIterations: Int = ARGON2KT_DEFAULT_T_COST,
        mCostInKibibyte: Int = ARGON2KT_DEFAULT_M_COST,
        parallelism: Int = ARGON2KT_DEFAULT_PARALLELISM,
        hashLengthInBytes: Int = ARGON2KT_DEFAULT_HASH_LENGTH,
        version: Argon2Version = ARGON2KT_DEFAULT_VERSION
    ): Argon2KtResult {
        val passwordBuffer = ByteBuffer.allocateDirect(password.size).put(password)
        val saltBuffer = ByteBuffer.allocateDirect(salt.size).put(salt)

        try {
            return hash(
                mode = mode,
                password = passwordBuffer,
                salt = saltBuffer,
                tCostInIterations = tCostInIterations,
                mCostInKibibyte = mCostInKibibyte,
                parallelism = parallelism,
                hashLengthInBytes = hashLengthInBytes,
                version = version
            )
        } finally {
            passwordBuffer.wipeDirectBuffer()
            saltBuffer.wipeDirectBuffer()
        }
    }

    @JvmOverloads
    fun hash(
        mode: Argon2Mode,
        password: ByteBuffer,
        salt: ByteBuffer,
        tCostInIterations: Int = ARGON2KT_DEFAULT_T_COST,
        mCostInKibibyte: Int = ARGON2KT_DEFAULT_M_COST,
        parallelism: Int = ARGON2KT_DEFAULT_PARALLELISM,
        hashLengthInBytes: Int = ARGON2KT_DEFAULT_HASH_LENGTH,
        version: Argon2Version = ARGON2KT_DEFAULT_VERSION
    ): Argon2KtResult = jni.argon2Hash(
        mode = mode.identifier,
        version = version.version,
        password = password,
        salt = salt,
        tCostInIterations = tCostInIterations,
        mCostInKibibyte = mCostInKibibyte,
        parallelism = parallelism,
        hashLengthInBytes = hashLengthInBytes
    )

    fun verify(
        mode: Argon2Mode,
        encoded: String,
        password: ByteArray
    ): Boolean {
        val passwordBuffer = ByteBuffer.allocateDirect(password.size).put(password)
        try {
            return verify(mode = mode, encoded = encoded, password = passwordBuffer)
        } finally {
            passwordBuffer.wipeDirectBuffer()
        }
    }

    fun verify(
        mode: Argon2Mode,
        encoded: String,
        password: ByteBuffer
    ): Boolean = jni.argon2Verify(
        mode = mode.identifier,
        encoded = encoded.toByteArray(charset = Charsets.US_ASCII),
        password = password
    )

    companion object {

        /**
         * Returns after loading and verifying the JNI implementation. It throws an IllegalStateException or similar
         * otherwise. As this method performs disk I/O (for loading the library) it should be run in the background and
         * not on the main thread.
         */
        fun assertJniWorking(soLoader: SoLoaderShim = SystemSoLoader()) =
            Argon2JniVerification(soLoader).assertJniWorking()
    }
}

class Argon2KtResult(val rawHash: ByteBuffer, val encodedOutput: ByteBuffer) {
    fun rawHashAsByteArray(): ByteArray = rawHash.toByteArray()
    fun rawHashAsHexadecimal(uppercase: Boolean = false): String = rawHashAsByteArray().encodeAsHex(uppercase)

    fun encodedOutputAsByteArray(): ByteArray = encodedOutput.toByteArray()
    fun encodedOutputAsString(): String = encodedOutputAsByteArray().toString(charset = Charsets.US_ASCII)
}
