// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class Argon2KtTest {

    @Test
    fun init_whenInitialized_thenNothingThrown() {
        Argon2Kt()
    }

    @Test
    fun hash_whenRunWithDifferentModes_thenMatchesExpectedOutputLength() {
        for (mode in Argon2Mode.values()) {
            val result = Argon2Kt().hash(
                mode = mode,
                password = "password".toByteArray(),
                salt = "somesalt".toByteArray(),
                tCostInIterations = 1,
                mCostInKibibyte = 1024,
                hashLengthInBytes = 32
            )
            assertThat(result.rawHashAsByteArray()).hasSize(32)
        }
    }

    @Test
    fun hash_whenSaltToShort_thenMatchingExceptionThrown() {
        try {
            Argon2Kt().hash(
                mode = Argon2Mode.ARGON2_I,
                password = "".toByteArray(),
                salt = "short".toByteArray()
            )
            assertThisLineNotExecuted()
        } catch (e: Argon2Exception) {
            assertThat(e.message).containsIgnoringCase("-6")
            assertThat(e.message).containsIgnoringCase("ARGON2_SALT_TOO_SHORT")
        }
    }

    //
    // Error state tests for verify
    //

    @Test
    fun officialTestSuite_v13_argon2i_errorCase_decodingFail1() {
        try {
            Argon2Kt().verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = "\$argon2i\$v=19\$m=65536,t=2,p=1c29tZXNhbHQ\$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA",
                password = "password".toByteArray()
            )
            assertThisLineNotExecuted()
        } catch (e: Argon2Exception) {
            assertThat(e.message).containsIgnoringCase("-32")
            assertThat(e.message).containsIgnoringCase("ARGON2_DECODING_FAIL")
        }
    }

    @Test
    fun officialTestSuite_v13_argon2i_errorCase_decodingFail2() {
        try {
            Argon2Kt().verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = "\$argon2i\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQwWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA",
                password = "password".toByteArray()
            )
            assertThisLineNotExecuted()
        } catch (e: Argon2Exception) {
            assertThat(e.message).containsIgnoringCase("-32")
            assertThat(e.message).containsIgnoringCase("ARGON2_DECODING_FAIL")
        }
    }

    @Test
    fun officialTestSuite_v13_argon2i_errorCase_saltTooShort() {
        try {
            Argon2Kt().verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = "\$argon2i\$v=19\$m=65536,t=2,p=1\$\$9sTbSlTio3Biev89thdrlKKiCaYsjjYVJxGAL3swxpQ",
                password = "password".toByteArray()
            )
            assertThisLineNotExecuted()
        } catch (e: Argon2Exception) {
            assertThat(e.message).containsIgnoringCase("-6")
            assertThat(e.message).containsIgnoringCase("ARGON2_SALT_TOO_SHORT")
        }
    }

    @Test
    fun officialTestSuite_v13_argon2i_errorCase_verifyMismatch() {
        val verificationResult = Argon2Kt().verify(
            mode = Argon2Mode.ARGON2_I,
            encoded = "\$argon2i\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQ\$8iIuixkI73Js3G1uMbezQXD0b8LG4SXGsOwoQkdAQIM",
            password = "password".toByteArray()
        )
        assertThat(verificationResult).isFalse()
    }

    //
    // V10, Argon2i
    //

    @Test
    fun officialTestSuite_v10_argon2i_case1() = runTest(
        version = Argon2Version.V10, t = 2, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "f6c4db4a54e2a370627aff3db6176b94a2a209a62c8e36152711802f7b30c694",
        expectedEncodedOutputHeader = "\$argon2i\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$9sTbSlTio3Biev89thdrlKKiCaYsjjYVJxGAL3swxpQ", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case2() = runTest(
        version = Argon2Version.V10, t = 2, m = 18, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "3e689aaa3d28a77cf2bc72a51ac53166761751182f1ee292e3f677a7da4c2467",
        expectedEncodedOutputHeader = "\$argon2i\$m=262144,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$Pmiaqj0op3zyvHKlGsUxZnYXURgvHuKS4/Z3p9pMJGc", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case3() = runTest(
        version = Argon2Version.V10, t = 2, m = 8, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "fd4dd83d762c49bdeaf57c47bdcd0c2f1babf863fdeb490df63ede9975fccf06",
        expectedEncodedOutputHeader = "\$argon2i\$m=256,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$/U3YPXYsSb3q9XxHvc0MLxur+GP960kN9j7emXX8zwY", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case4() = runTest(
        version = Argon2Version.V10, t = 2, m = 8, p = 2,
        password = "password", salt = "somesalt",
        expectedHexHash = "b6c11560a6a9d61eac706b79a2f97d68b4463aa3ad87e00c07e2b01e90c564fb",
        expectedEncodedOutputHeader = "\$argon2i\$m=256,t=2,p=2\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$tsEVYKap1h6scGt5ovl9aLRGOqOth+AMB+KwHpDFZPs", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case5() = runTest(
        version = Argon2Version.V10, t = 1, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "81630552b8f3b1f48cdb1992c4c678643d490b2b5eb4ff6c4b3438b5621724b2",
        expectedEncodedOutputHeader = "\$argon2i\$m=65536,t=1,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$gWMFUrjzsfSM2xmSxMZ4ZD1JCytetP9sSzQ4tWIXJLI", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case6() = runTest(
        version = Argon2Version.V10, t = 4, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "f212f01615e6eb5d74734dc3ef40ade2d51d052468d8c69440a3a1f2c1c2847b",
        expectedEncodedOutputHeader = "\$argon2i\$m=65536,t=4,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$8hLwFhXm6110c03D70Ct4tUdBSRo2MaUQKOh8sHChHs", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case7() = runTest(
        version = Argon2Version.V10, t = 2, m = 16, p = 1,
        password = "differentpassword", salt = "somesalt",
        expectedHexHash = "e9c902074b6754531a3a0be519e5baf404b30ce69b3f01ac3bf21229960109a3",
        expectedEncodedOutputHeader = "\$argon2i\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$6ckCB0tnVFMaOgvlGeW69ASzDOabPwGsO/ISKZYBCaM", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v10_argon2i_case8() = runTest(
        version = Argon2Version.V10, t = 2, m = 16, p = 1,
        password = "password", salt = "diffsalt",
        expectedHexHash = "79a103b90fe8aef8570cb31fc8b22259778916f8336b7bdac3892569d4f1c497",
        expectedEncodedOutputHeader = "\$argon2i\$m=65536,t=2,p=1\$ZGlmZnNhbHQ",
        expectedEncodedOutputHash = "\$eaEDuQ/orvhXDLMfyLIiWXeJFvgza3vaw4kladTxxJc", mode = Argon2Mode.ARGON2_I
    )

    //
    // V13, Argon2i
    //

    @Test
    fun officialTestSuite_v13_argon2i_case1() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "c1628832147d9720c5bd1cfd61367078729f6dfb6f8fea9ff98158e0d7816ed0",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$wWKIMhR9lyDFvRz9YTZweHKfbftvj+qf+YFY4NeBbtA", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case2() = runTest(
        version = Argon2Version.V13, t = 2, m = 18, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "296dbae80b807cdceaad44ae741b506f14db0959267b183b118f9b24229bc7cb",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=262144,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$KW266AuAfNzqrUSudBtQbxTbCVkmexg7EY+bJCKbx8s", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case3() = runTest(
        version = Argon2Version.V13, t = 2, m = 8, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "89e9029f4637b295beb027056a7336c414fadd43f6b208645281cb214a56452f",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=256,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$iekCn0Y3spW+sCcFanM2xBT63UP2sghkUoHLIUpWRS8", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case4() = runTest(
        version = Argon2Version.V13, t = 2, m = 8, p = 2,
        password = "password", salt = "somesalt",
        expectedHexHash = "4ff5ce2769a1d7f4c8a491df09d41a9fbe90e5eb02155a13e4c01e20cd4eab61",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=256,t=2,p=2\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$T/XOJ2mh1/TIpJHfCdQan76Q5esCFVoT5MAeIM1Oq2E", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case5() = runTest(
        version = Argon2Version.V13, t = 1, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "d168075c4d985e13ebeae560cf8b94c3b5d8a16c51916b6f4ac2da3ac11bbecf",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=65536,t=1,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$0WgHXE2YXhPr6uVgz4uUw7XYoWxRkWtvSsLaOsEbvs8", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case6() = runTest(
        version = Argon2Version.V13, t = 4, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "aaa953d58af3706ce3df1aefd4a64a84e31d7f54175231f1285259f88174ce5b",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=65536,t=4,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$qqlT1YrzcGzj3xrv1KZKhOMdf1QXUjHxKFJZ+IF0zls", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case7() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "differentpassword", salt = "somesalt",
        expectedHexHash = "14ae8da01afea8700c2358dcef7c5358d9021282bd88663a4562f59fb74d22ee",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$FK6NoBr+qHAMI1jc73xTWNkCEoK9iGY6RWL1n7dNIu4", mode = Argon2Mode.ARGON2_I
    )

    @Test
    fun officialTestSuite_v13_argon2i_case8() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "password", salt = "diffsalt",
        expectedHexHash = "b0357cccfbef91f3860b0dba447b2348cbefecadaf990abfe9cc40726c521271",
        expectedEncodedOutputHeader = "\$argon2i\$v=19\$m=65536,t=2,p=1\$ZGlmZnNhbHQ",
        expectedEncodedOutputHash = "\$sDV8zPvvkfOGCw26RHsjSMvv7K2vmQq/6cxAcmxSEnE", mode = Argon2Mode.ARGON2_I
    )

    //
    // V13, Argon2id
    //

    @Test
    fun officialTestSuite_v13_argon2id_case1() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "09316115d5cf24ed5a15a31a3ba326e5cf32edc24702987c02b6566f61913cf7",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case2() = runTest(
        version = Argon2Version.V13, t = 2, m = 18, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "78fe1ec91fb3aa5657d72e710854e4c3d9b9198c742f9616c2f085bed95b2e8c",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=262144,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$eP4eyR+zqlZX1y5xCFTkw9m5GYx0L5YWwvCFvtlbLow", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case3() = runTest(
        version = Argon2Version.V13, t = 2, m = 8, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "9dfeb910e80bad0311fee20f9c0e2b12c17987b4cac90c2ef54d5b3021c68bfe",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=256,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$nf65EOgLrQMR/uIPnA4rEsF5h7TKyQwu9U1bMCHGi/4", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case4() = runTest(
        version = Argon2Version.V13, t = 2, m = 8, p = 2,
        password = "password", salt = "somesalt",
        expectedHexHash = "6d093c501fd5999645e0ea3bf620d7b8be7fd2db59c20d9fff9539da2bf57037",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=256,t=2,p=2\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$bQk8UB/VmZZF4Oo79iDXuL5/0ttZwg2f/5U52iv1cDc", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case5() = runTest(
        version = Argon2Version.V13, t = 1, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "f6a5adc1ba723dddef9b5ac1d464e180fcd9dffc9d1cbf76cca2fed795d9ca98",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=65536,t=1,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$9qWtwbpyPd3vm1rB1GThgPzZ3/ydHL92zKL+15XZypg", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case6() = runTest(
        version = Argon2Version.V13, t = 4, m = 16, p = 1,
        password = "password", salt = "somesalt",
        expectedHexHash = "9025d48e68ef7395cca9079da4c4ec3affb3c8911fe4f86d1a2520856f63172c",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=65536,t=4,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$kCXUjmjvc5XMqQedpMTsOv+zyJEf5PhtGiUghW9jFyw", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case7() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "differentpassword", salt = "somesalt",
        expectedHexHash = "0b84d652cf6b0c4beaef0dfe278ba6a80df6696281d7e0d2891b817d8c458fde",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=65536,t=2,p=1\$c29tZXNhbHQ",
        expectedEncodedOutputHash = "\$C4TWUs9rDEvq7w3+J4umqA32aWKB1+DSiRuBfYxFj94", mode = Argon2Mode.ARGON2_ID
    )

    @Test
    fun officialTestSuite_v13_argon2id_case8() = runTest(
        version = Argon2Version.V13, t = 2, m = 16, p = 1,
        password = "password", salt = "diffsalt",
        expectedHexHash = "bdf32b05ccc42eb15d58fd19b1f856b113da1e9a5874fdcc544308565aa8141c",
        expectedEncodedOutputHeader = "\$argon2id\$v=19\$m=65536,t=2,p=1\$ZGlmZnNhbHQ",
        expectedEncodedOutputHash = "\$vfMrBczELrFdWP0ZsfhWsRPaHppYdP3MVEMIVlqoFBw", mode = Argon2Mode.ARGON2_ID
    )
}

private fun runTest(
    version: Argon2Version,
    t: Int,
    m: Int,
    p: Int,
    password: String,
    salt: String,
    expectedHexHash: String,
    expectedEncodedOutputHeader: String,
    expectedEncodedOutputHash: String,
    mode: Argon2Mode
) {
    val hashResult = Argon2Kt().hash(
        mode = mode,
        password = password.toByteArray(),
        salt = salt.toByteArray(),
        tCostInIterations = t,
        mCostInKibibyte = 1.shl(m),
        parallelism = p,
        hashLengthInBytes = 32,
        version = version
    )

    assertThat(hashResult.rawHashAsHexadecimal()).isEqualTo(expectedHexHash)
    if (version == ARGON2KT_DEFAULT_VERSION) {
        // the version string does not match for V10 (see test.c in original argon2 repo)
        assertThat(hashResult.encodedOutputAsString()).isEqualTo(expectedEncodedOutputHeader + expectedEncodedOutputHash)
    }

    val verificationResult = Argon2Kt().verify(
        mode = mode,
        encoded = hashResult.encodedOutputAsString(),
        password = password.toByteArray()
    )
    assertThat(verificationResult).isTrue()
}

private fun assertThisLineNotExecuted() = fail<Void>("This line shouldn't be executed (most likely expected exception)")
