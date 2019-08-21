// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import java.nio.ByteBuffer
import java.util.*

/**
 * Decodes a hexadecimal String. Will throw if it encounters illegal characters (i.e. not 0-9a-fA-F) or if the String
 * has an odd length.
 */
@ExperimentalUnsignedTypes
fun String.decodeAsHex(): ByteArray {
    checkArgument(this.length % 2 == 0, "A valid hex string must have an even number of characters")

    return ByteArray(this.length / 2) {
        this.substring(2 * it, 2 * it + 2).toUByte(radix = 16).toByte()
    }
}

/** Encodes a byte array into a hexadecimal String. */
fun ByteArray.encodeAsHex(uppercase: Boolean = true): String {
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
 */
fun ByteBuffer.wipeDirectBuffer(random: Random = Random()) {
    if (!this.isDirect) throw IllegalStateException("Only direct-allocated byte buffers can be meaningfully wiped")

    val arr = ByteArray(this.capacity())
    this.rewind()

    // overwrite bytes (actually overwrites the memory since it is a direct buffer)
    random.nextBytes(arr)
    this.put(arr)
}

/** If the assertion holds nothing happens. Otherwise, an IllegalArgumenException is thrown with the given message. */
internal fun checkArgument(assertion: Boolean, message: String) {
    if (!assertion) throw IllegalArgumentException(message)
}
