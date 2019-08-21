// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import org.junit.Test

class Argon2JniTest {

    @Test
    fun init_whenInitialized_thenNothingThrown() {
        Argon2Jni(SystemSoLoader())
    }
}

class Argon2JniVerificationTest {

    @Test
    fun assertJniWorking_whenRun_thenReturnsWithoutException() {
        Argon2JniVerification(SystemSoLoader()).assertJniWorking()
    }
}
