// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.lang.IllegalArgumentException

class Argon2ErrorTest {

    @Test
    fun argon2Exception_whenCreatedFromCode_thenContainsMessageAndCode() {
        val error = Argon2Exception.fromErrorCode(-6)

        assertThat(error.message).contains("ARGON2_SALT_TOO_SHORT")
        assertThat(error.message).contains("-6")
    }

    @Test(expected = IllegalArgumentException::class)
    fun argon2Exception_whenCreatedFromUnknownErrorCode_thenThrows() {
        Argon2Exception.fromErrorCode(123456)
    }
}
