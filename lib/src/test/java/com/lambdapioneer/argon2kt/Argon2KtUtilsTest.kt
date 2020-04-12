// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Argon2KtUtilsTest {

    @Test
    fun decode_whenEmptyString_thenEmptyArray() {
        assertThat("".decodeAsHex()).isEmpty()
    }

    @Test(expected = IllegalArgumentException::class)
    fun decode_whenOddNumberChars_thenThrows() {
        assertThat("1".decodeAsHex())
    }

    @Test(expected = IllegalArgumentException::class)
    fun decode_whenOutOfBoundsChars_thenThrows() {
        assertThat("1Z".decodeAsHex())
    }

    @Test
    fun encode_whenEmptyArray_themEmptyString() {
        assertThat(byteArrayOf().encodeAsHex()).isEmpty()
    }

    @Test
    fun encode_whenUpperCaseTrue_thenStringUppercase() {
        assertThat(byteArrayOf(0x1A).encodeAsHex(uppercase = true)).isEqualTo("1A")
    }

    @Test
    fun encode_whenUpperCaseFalse_thenStringLowercase() {
        assertThat(byteArrayOf(0x1A).encodeAsHex(uppercase = false)).isEqualTo("1a")
    }

    @Test
    fun encodeDecode_whenPerformingVarious_thenIterativeEncodeDecodeYieldSameResult() {
        fun verifyIdentity(hexString: String) {
            val mangledVersion = hexString
                .decodeAsHex()
                .encodeAsHex(uppercase = false)
                .decodeAsHex()
                .encodeAsHex(uppercase = true)

            assertThat(mangledVersion).isEqualTo(hexString)
        }

        verifyIdentity("00")
        verifyIdentity("FF")
        verifyIdentity("ABCDEF")
        verifyIdentity("00ABCDEF00")
    }
}
