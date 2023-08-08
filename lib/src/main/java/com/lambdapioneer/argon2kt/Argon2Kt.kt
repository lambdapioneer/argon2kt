// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import java.nio.ByteBuffer

internal const val ARGON2KT_DEFAULT_T_COST = 1 // number of iterations
internal const val ARGON2KT_DEFAULT_M_COST = 65536 // resulting in 64 MiB memory cost
internal const val ARGON2KT_DEFAULT_PARALLELISM = 2 // running two threads in parallel
internal const val ARGON2KT_DEFAULT_HASH_LENGTH = 32 // resulting in 32 bytes = 256 bit output
internal val ARGON2KT_DEFAULT_VERSION = Argon2Version.V13 // default to newest version V13

/**
 * The different Argon2 modes that differ regarding side-channel-and-memory-tradeoffs. Please refer to the documentation
 * of the Argon2 project for details.
 */
enum class Argon2Mode(val identifier: Int) {

    /**
     * Argon2d chooses memory depending on the password and salt. Not suitable for environments
     * with potential side-channel attacks.
     */
    ARGON2_D(0),

    /**
     * Argon2i chooses memory independent of the password and salt reducing the risk from side-channels. However, the
     * memory trade-off is weaker.
     */
    ARGON2_I(1),

    /**
     * Argon2id combines the Argon2d and Argon2i providing a reasonable trade-off between memory dependence and side-
     * channels.
     */
    ARGON2_ID(2)
}

/**
 * The Argon2 version to use. It's preferred to use the latest (V13) version where suitable.
 */
enum class Argon2Version(val version: Int) {

    /** The version V10) */
    V10(0x10),

    /** The (default) version V13) */
    V13(0x13)
}

/**
 * The main entry point for the Argon2Kt library. The initialization will execute the load of the .so library (native
 * C code). Therefore, it should be initialized off the main thread.
 *
 * @param soLoader Callback for loading the .so library. Defaults to the SystemSoLoader. However, one can easily replace
 *                 it with e.g. the ReLinker library for more robust .so loading.
 */
class Argon2Kt(soLoader: SoLoaderShim = SystemSoLoader()) {
    private val jni = Argon2Jni(soLoader)

    /**
     * Computes the Argon2 hash for the given parameters. Using direct-allocated ByteBuffers is the preferred method as
     * they can be deterministically wiped afterwards.
     *
     * @param mode The Argon2Mode to use.
     * @param password The password.
     * @param salt The password.
     * @param tCostInIterations The computational cost in iterations. The application should choose this to fit a
     *                          certain amount of time after fixing the m_cost.
     * @param mCostInKibibyte The memory cost in KibiByte (i.e. 1024 byte).
     * @param hashLengthInBytes The length of the raw hash in bytes. I.e. for a 512 bit output choose 64.
     * @param version The Argon2Version to use.
     *
     * @return The Argon2KtResult exposes both the raw hash and the encoded string representation.
     *
     * @throws Argon2Exception for parameter and runtime errors.
     */
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

    /**
     * Computes the Argon2 hash for the given parameters. Using this method will cause ByteArrays to be allocated on the
     * JVM heap which are hard to clear. It's suggested that you always handle secrets through direct-allocated
     * ByteBuffers.
     *
     * @param mode The Argon2Mode to use.
     * @param password The password (must be a ByteBuffer that has been allocated as direct).
     * @param salt The password (must be a ByteBuffer that has been allocated as direct).
     * @param tCostInIterations The computational cost in iterations. The application should choose this to fit a
     *                          certain amount of time after fixing the m_cost.
     * @param mCostInKibibyte The memory cost in KibiByte (i.e. 1024 byte).
     * @param hashLengthInBytes The length of the raw hash in bytes. I.e. for a 512 bit output choose 64.
     * @param version The Argon2Version to use.
     *
     * @return The Argon2KtResult exposes both the raw hash and the encoded string representation.
     *
     * @throws Argon2Exception for parameter and runtime errors.
     */
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

    /**
     * Verifies a given password against the encoded string representation. As the encoded string already contains the
     * parameter information (e.g. t_cost, m_cost) this is preferred over storing the raw hash.
     *
     * @param mode The Argon2Mode to use.
     * @param encoded The encoded string representation retrieved from a Argon2KtResult or an external source.
     * @param password The password to verify against the encoded string representation.
     *
     * @throws Argon2Exception for parameter and runtime errors.
     */
    fun verify(
        mode: Argon2Mode,
        encoded: String,
        password: ByteBuffer
    ): Boolean = jni.argon2Verify(
        mode = mode.identifier,
        encoded = encoded.toByteArray(charset = Charsets.US_ASCII),
        password = password
    )

    /**
     * Verifies a given password against the encoded string representation. As the encoded string already contains the
     * parameter information (e.g. t_cost, m_cost) this is preferred over storing the raw hash.
     *
     * One should prefer the verify version accepting a ByteArray as it will allow to wipe the secret from memory.
     *
     * @param mode The Argon2Mode to use.
     * @param encoded The encoded string representation retrieved from a Argon2KtResult or an external source.
     * @param password The password to verify against the encoded string representation.
     *
     * @throws Argon2Exception for parameter and runtime errors.
     */
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

/**
 * Result from the Argon2Kt.hash method containing both the rawHash and encodedOutput as direct-allocated ByteBuffers.
 */
class Argon2KtResult(val rawHash: ByteBuffer, val encodedOutput: ByteBuffer) {

    /** The raw hash as a ByteArray. */
    fun rawHashAsByteArray(): ByteArray = rawHash.toByteArray()

    /** The raw hash as a String encoded in hexadecimal. */
    fun rawHashAsHexadecimal(uppercase: Boolean = false): String = rawHashAsByteArray().encodeAsHex(uppercase)

    /** The encoded output string as a ByteArray. */
    fun encodedOutputAsByteArray(): ByteArray = encodedOutput.toByteArray()

    /** The encoded output string as a Base64 String using $ as delimiters. */
    fun encodedOutputAsString(): String = encodedOutputAsByteArray().toString(charset = Charsets.US_ASCII)
}
