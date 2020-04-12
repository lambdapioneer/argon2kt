// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Argon2KtBenchmarkUtilsInstrumentedTest {

    @Test
    fun searchIterationCountForArgon2_whenGivenSensibleConfiguration_thenResultSensible() {
        // As we cannot make assumptions about the tested device, we will just make sure that the returned iteration
        // count is in a sensible range. See the "Argon2KtUtilsUnitTest" for white-box tests of the underlying logic.
        val iterationsCount = Argon2KtBenchmark.searchIterationCount(
            argon2Kt = Argon2Kt(),
            argon2Mode = Argon2Mode.ARGON2_ID,
            targetTimeMs = 1000,
            mCostInKibibyte = 4096 // only 4 MiB to speed-up testing
        )

        // The returned iterationsCount ~14 for my Pixel 3; ~80 for x86 emulator
        assertThat(iterationsCount).isGreaterThan(2)
        assertThat(iterationsCount).isLessThan(128)
    }

}
