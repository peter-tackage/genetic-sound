package com.petertackage.geneticsound

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

/**
 * A MutationProbability calculator that determines the mutation rate based upon the population fitness statistics.
 *
 * The motivation is to better promote discovery when fitness level converge to prevent the algorithm focusing on local
 * maxima.
 *
 * The downside being that depending on the configuration, it may be overly disruptive to convergence later after the
 * instability of the early generations has passed.
 */
class VarianceMutationProbability(val stats: DescriptiveStatistics,
                                  val baseProbability: Float,
                                  maxProbability: Float,
                                  val cvThresholdPercent: Float) : MutationProbability {

    private val deltaProbability = maxProbability - baseProbability

    init {
        if (maxProbability < baseProbability) throw IllegalArgumentException("maxProbability must be more that baseProbability")
    }

    override fun next(): Float {

        val sd = stats.standardDeviation
        val mean = stats.mean

        // Smaller means less variance, therefore should increase the mutation rate.

        // Have low threshold  and scale up to max
        val cvPercent = (sd / mean).toFloat() * 100F
        return scaledProbability(cvPercent)
    }

    private fun scaledProbability(cvPercent: Float): Float {
        return baseProbability + deltaProbability * (1 - cvPercent / cvThresholdPercent)
    }
}
