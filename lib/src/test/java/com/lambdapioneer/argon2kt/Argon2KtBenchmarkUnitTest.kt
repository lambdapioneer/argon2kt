// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Argon2KtBenchmarkUnitTest {

    @Test(expected = IllegalArgumentException::class)
    fun searchIterationCountForMetric_whenMetricReturns0_thenThrows() {
        searchIterationCountForMetric(1000) { it.toLong() * 0 }
    }

    @Test
    fun searchIterationCountForMetric_whenOneIterationAboveTarget_thenReturns1() {
        val iterations = searchIterationCountForMetric(1000) { it.toLong() * 2000 }
        assertThat(iterations).isEqualTo(1)
    }

    @Test
    fun searchIterationCountForMetric_whenOneIterationExactlyTarget_thenReturns2() {
        val iterations = searchIterationCountForMetric(1000) { it.toLong() * 1000 }
        assertThat(iterations).isEqualTo(2)
    }

    @Test
    fun searchIterationCountForMetric_whenIterationIsJustAboveTenthOfTarget_thenReturns10() {
        val iterations = searchIterationCountForMetric(1000) { it.toLong() * 101 }
        assertThat(iterations).isEqualTo(10)
    }

    @Test
    fun searchIterationCountForMethod_whenActualSleep_thenWorksCorrectly() {
        val iterations = searchIterationCountForMethod(500) { Thread.sleep(it.toLong() * 101) }
        assertThat(iterations).isEqualTo(5)
    }
}
