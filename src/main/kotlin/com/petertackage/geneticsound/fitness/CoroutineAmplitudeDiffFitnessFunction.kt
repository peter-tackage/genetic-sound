package com.petertackage.geneticsound.fitness

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

/**
 * Grossly inefficient use of Kotlin coroutines to perform amplitude difference calculation.
 *
 * From my observations, the overhead of the coroutine is too big for the simple diff calculation that is being performed.
 */
class CoroutineAmplitudeDiffFitnessFunction : FitnessFunction {
    override fun compare(target: ShortArray, proposed: ShortArray): Long {

        val deferred = proposed.mapIndexed { index, proposedFrame ->
            async(CommonPool) { calculateDiff(target[index], proposedFrame).toLong() }
        }

        return runBlocking {
            deferred.sumExactByLong { it.await() }
        }

    }

    private fun calculateDiff(targetFrame: Short, proposedFrame: Short): Int {
        // Need to use Int to as diff of Short.MAX_VALUE and Short.MIN_VALUE can't be represented in Short.
        return Math.abs(targetFrame - proposedFrame)
    }

    // Represent the diff in Long (64bits) to allow for the greatest resolution when determining fitness
    // If we clip the diff at Short.MAX/MIN or Int.MAX/MIN then we won't see improvements in the algorithm when
    // the population is unfit.

    inline fun <T> Iterable<T>.sumExactByLong(selector: (T) -> Long): Long {
        var sum = 0L
        for (element in this) {
            try {
                sum = Math.addExact(sum, selector(element))
            } catch (exp: ArithmeticException) {
                sum = Long.MAX_VALUE
            }
        }
        return sum
    }

}