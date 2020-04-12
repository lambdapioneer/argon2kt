// Copyright (c) Daniel Hugenroth
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.lambdapioneer.argon2kt

import kotlin.math.ceil

/**
 * Utils class to help determining Argon2 parameters.
 */
class Argon2KtBenchmark private constructor() {

    companion object {

        /**
         * Returns an iteration count for the given configuration that makes Argon2 take just above the given "targetTimeMs".
         * Note that there can be vast differences between devices and debug/release builds.
         *
         * Do not rely on this method to make claims on "how long it would take to crack a password". However, it is helpful
         * to choose an iteration count that provides sensible/convenient speed for a given configuration.
         */
        fun searchIterationCount(
            argon2Kt: Argon2Kt,
            argon2Mode: Argon2Mode,
            targetTimeMs: Long,
            mCostInKibibyte: Int = ARGON2KT_DEFAULT_M_COST,
            parallelism: Int = ARGON2KT_DEFAULT_PARALLELISM,
            hashLengthInBytes: Int = ARGON2KT_DEFAULT_HASH_LENGTH,
            version: Argon2Version = ARGON2KT_DEFAULT_VERSION
        ): Int =
            searchIterationCountForMethod(targetTimeMs) { tCostInIterations ->
                argon2Kt.hash(
                    mode = argon2Mode,
                    password = "dummypassword".toByteArray(),
                    salt = "dummysalt".toByteArray(),
                    tCostInIterations = tCostInIterations,
                    mCostInKibibyte = mCostInKibibyte,
                    parallelism = parallelism,
                    hashLengthInBytes = hashLengthInBytes,
                    version = version
                )
            }
    }
}

/** See [searchIterationCountForMetric] */
internal fun searchIterationCountForMethod(targetTimeMs: Long, methodToMeasure: (Int) -> Unit) =
    searchIterationCountForMetric(
        targetTimeMs
    ) {
        val start = System.nanoTime()
        methodToMeasure(it)
        (System.nanoTime() - start) / 1000000
    }

/**
 * Returns an iteration count that results in the "measureTimeMs" to take more than "targetTimeMs". It assumes that the
 * measured time increases (roughly) proportionally with the number of iterations.
 */
internal fun searchIterationCountForMetric(
    targetTimeMs: Long,
    iterationToMetric: (Int) -> Long
): Int {
    var iterations = 1
    var iterationsTime = iterationToMetric(iterations)

    while (iterationsTime <= targetTimeMs) {
        checkArgument(iterationsTime > 0, "The to-be measured method must always take >0ms\"")
        val timePerIteration = iterationsTime.toFloat() / iterations.toFloat()

        // approximate assuming proportional relationship: iterations ~ time
        val newIterations = ceil(targetTimeMs.toFloat() / timePerIteration).toInt()

        iterations = if (newIterations <= iterations)
            newIterations + 1 // avoid infinite loop by growing strictly monotonically
        else
            newIterations

        iterationsTime = iterationToMetric(iterations)
    }

    return iterations
}
