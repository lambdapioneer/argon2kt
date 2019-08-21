// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import androidx.annotation.Keep
import java.nio.ByteBuffer

@Keep
internal class Argon2Jni(soLoader: SoLoaderShim) {

    init {
        soLoader.loadLibrary("argon2jni")
    }

    fun argon2Hash(
        mode: Int,
        version: Int,
        password: ByteBuffer,
        salt: ByteBuffer,
        tCostInIterations: Int,
        mCostInKibibyte: Int,
        parallelism: Int,
        hashLengthInBytes: Int
    ): Argon2KtResult {
        checkArgument(mode in 0..2, "mode must be in range 0..2")
        checkArgument(version in arrayListOf(0x10, 0x13), "version must be either 0x10 or 0x13")

        checkArgument(tCostInIterations > 0, "tCostInIterations must be greater than 0")
        checkArgument(mCostInKibibyte > 0, "mCostInKibibyte must be greater than 0")
        checkArgument(parallelism > 0, "parallelism must be greater than 0")
        checkArgument(hashLengthInBytes > 0, "hashLengthInBytes must be greater than 0")

        checkArgument(password.isDirect, "the password bytebuffer must be allocated as direct")
        checkArgument(salt.isDirect, "the salt bytebuffer must be allocated as direct")

        val hashTarget = ByteBufferTarget()
        val encodedTarget = ByteBufferTarget()

        val returnCode = nativeArgon2Hash(
            mode = mode,
            version = version,
            t_cost = tCostInIterations,
            m_cost = mCostInKibibyte,
            parallelism = parallelism,
            password = password,
            salt = salt,
            hash_length = hashLengthInBytes,
            hash_destination = hashTarget,
            encoded_destination = encodedTarget
        )

        val argonError = Argon2Error.fromErrorCode(returnCode)
        if (argonError != Argon2Error.ARGON2_OK) {
            throw Argon2Exception.fromErrorCode(returnCode)
        }

        if (!hashTarget.hasByteBufferSet()) {
            throw Argon2Exception("Argon2 call did not set hash byte buffer")
        }

        return Argon2KtResult(
            rawHash = hashTarget.getByteBuffer(),
            encodedOutput = encodedTarget.dropLastN(1).getByteBuffer() // ignore trailing \0 byte
        )
    }

    fun argon2Verify(mode: Int, encoded: ByteArray, password: ByteBuffer): Boolean {
        checkArgument(mode in 0..2, "mode must be in range 0..2")
        checkArgument(password.isDirect, "the password bytebuffer must be allocated as direct")

        val encodedBuffer = ByteBuffer.allocateDirect(encoded.size + 1)
            .put(encoded)
            .put(0x00) // native method expects a \0 terminated string

        val returnCode = nativeArgon2Verify(
            mode = mode,
            encoded = encodedBuffer,
            password = password
        )

        return when (Argon2Error.fromErrorCode(returnCode)) {
            Argon2Error.ARGON2_OK -> true
            Argon2Error.ARGON2_VERIFY_MISMATCH -> false
            else -> throw Argon2Exception.fromErrorCode(returnCode)
        }
    }

    @Keep
    private external fun nativeArgon2Hash(
        mode: Int,
        version: Int,
        t_cost: Int,
        m_cost: Int,
        parallelism: Int,
        password: ByteBuffer,
        salt: ByteBuffer,
        hash_length: Int,
        hash_destination: ByteBufferTarget,
        encoded_destination: ByteBufferTarget
    ): Int

    @Keep
    private external fun nativeArgon2Verify(
        mode: Int,
        encoded: ByteBuffer,
        password: ByteBuffer
    ): Int
}

/** Helper class to verify that the native library can be loaded and used */
internal class Argon2JniVerification(private val soLoader: SoLoaderShim) {

    fun assertJniWorking() {
        soLoader.loadLibrary("argon2jni")
        if (verifyJniByAddingOne(100) != 101) {
            throw IllegalStateException("JNI check failed")
        }
    }

    @Keep
    private external fun verifyJniByAddingOne(input: Int): Int
}
