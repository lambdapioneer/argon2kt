// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import androidx.annotation.Keep
import java.nio.ByteBuffer

/**
 * Returns a copy of the byte buffer's content as a ByteArray.
 */
internal fun ByteBuffer.toByteArray(): ByteArray {
    val arr = ByteArray(this.capacity())
    this.rewind()
    this.get(arr)
    return arr
}

/**
 * Class that allows setting a byte buffer to it. Used as a callback for JNI methods to return multiple outputs without
 * having to deal with the complexity of instantiating classes in JNI.
 */
@Keep
internal class ByteBufferTarget {

    @Keep
    private var byteBuffer: ByteBuffer? = null

    fun hasByteBufferSet() = byteBuffer != null

    /**
     * This removes the given number of trailing bytes from the underlying byte buffer. It does so by allocating a new
     * direct byte buffer, then copying over all but the last N items, and then wiping the previous byte buffer.
     *
     * @throws NullPointerException if there's no byte buffer set yet
     *
     * @return This object to allow chaining of operations. The ByteBufferTarget is not copied.
     */
    fun dropLastN(dropLastN: Int = 0): ByteBufferTarget {
        checkArgument(dropLastN >= 0, "dropLastN must not be negative")

        val oldByteBuffer = byteBuffer!!
        val newLength = oldByteBuffer.capacity() - dropLastN
        try {
            byteBuffer = ByteBuffer.allocateDirect(newLength)

            // copying one-by-one to avoid byte array allocation on heap
            oldByteBuffer.rewind()
            repeat(newLength) {
                byteBuffer!!.put(oldByteBuffer.get())
            }
        } finally {
            oldByteBuffer.wipeDirectBuffer()
        }

        return this
    }

    /**
     * Returns the underlying byte buffer. Caller should check that a byte buffer exists as this method throws an NPE
     * otherwise.
     */
    fun getByteBuffer(): ByteBuffer = byteBuffer!!
}
