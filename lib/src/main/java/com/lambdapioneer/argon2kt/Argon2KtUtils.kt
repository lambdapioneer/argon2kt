// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import java.nio.ByteBuffer
import java.util.*

/**
 * Decodes a [String] holding a hexadecimal encoding as a [ByteArray].
 *
 * @return [ByteArray] which length is half the string's length.
 *
 * @throw [IllegalAccessException] Will throw if it encounters illegal characters (i.e. not 0-9a-fA-F) or if the String
 * has an odd length.
 */
internal fun String.decodeAsHex(): ByteArray {
    checkArgument(this.length % 2 == 0, "A valid hex string must have an even number of characters")

    return ByteArray(this.length / 2) {
        this.substring(2 * it, 2 * it + 2).toInt(radix = 16).toByte()
    }
}

/**
 *  Encodes a byte array into a hexadecimal encoded String.
 *
 *  @param uppercase If true uppercase letters are used (A..F), otherwise lowercase letters are used (a..f).
 *
 *  @return [String] which length the twice the [ByteArray]'s length.
 */
internal fun ByteArray.encodeAsHex(uppercase: Boolean = true): String {
    val sb = java.lang.StringBuilder(size * 2)
    val formatString = if (uppercase) "%02X" else "%02x"

    for (b in this) {
        sb.append(String.format(formatString, b))
    }

    return sb.toString()
}

/**
 * Overwrites the bytes of a byte buffer with random bytes. The method asserts that the buffer is a direct buffer as a
 * precondition.
 *
 * @param random The random generator to use for overwriting. Default's to Java's standard [Random] implementation.
 * However, you might want to use a [java.security.SecureRandom] source for more adverse threat models.
 *
 * @throws [IllegalStateException] if the buffer [ByteBuffer.isDirect] is false.
 */
internal fun ByteBuffer.wipeDirectBuffer(random: Random = Random()) {
    if (!this.isDirect) throw IllegalStateException("Only direct-allocated byte buffers can be meaningfully wiped")

    val arr = ByteArray(this.capacity())
    this.rewind()

    // overwrite bytes (actually overwrites the memory since it is a direct buffer)
    random.nextBytes(arr)
    this.put(arr)
}

/** If the assertion holds nothing happens. Otherwise, an IllegalArgumentException is thrown with the given message. */
internal fun checkArgument(assertion: Boolean, message: String) {
    if (!assertion) throw IllegalArgumentException(message)
}

/**
 * Util class with helper methods for dealing with HEX encodings and [ByteBuffer] objects.
 */
class Argon2KtUtils private constructor() {

    companion object {

        /**
         * Decodes a [String] holding a hexadecimal encoding as a [ByteArray].
         *
         * @param string The string holding the hexadecimal encoding.
         *
         * @return [ByteArray] which length is half the string's length.
         *
         * @throw [IllegalAccessException] Will throw if it encounters illegal characters (i.e. not 0-9a-fA-F) or if the String
         * has an odd length.
         */
        fun decodeAsHex(string: String): ByteArray = string.decodeAsHex()

        /**
         *  Encodes a byte array into a hexadecimal encoded String.
         *
         *  @param byteArray The [ByteArray] to convert.
         *  @param uppercase If true uppercase letters are used (A..F), otherwise lowercase letters are used (a..f).
         *
         *  @return [String] which length the twice the [ByteArray]'s length.
         */
        fun ByteArray.encodeAsHex(byteArray: ByteArray, uppercase: Boolean = true) =
            byteArray.encodeAsHex(uppercase)


        /**
         * Overwrites the bytes of a byte buffer with random bytes. The method asserts that the buffer is a direct buffer as a
         * precondition.
         *
         * @param byteBuffer THe [ByteBuffer] to overwrite. Must be directly allocated.
         * @param random The random generator to use for overwriting. Default's to Java's standard [Random] implementation.
         * However, you might want to use a [java.security.SecureRandom] source for more adverse threat models.
         *
         * @throws [IllegalStateException] if the buffer [ByteBuffer.isDirect] is false.
         */
        fun ByteBuffer.wipeDirectBuffer(byteBuffer: ByteBuffer, random: Random = Random()) =
            byteBuffer.wipeDirectBuffer(random)
    }
}
