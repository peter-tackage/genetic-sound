package com.petertackage.geneticsound.mutation

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

/**
 * A MutationProbability calculator that determines the mutation rate based upon the population fitness statistics.
 *
 * Once the co-efficient of variance (CV) reaches a threshold (cvThresholdPercent), the mutation rate increases to a maximum.
 *
 * The motivation is to better promote discovery when fitness levels converge to prevent the algorithm focusing on local
 * maxima.
 *
 * The downside being that depending on the configuration, it may be overly disruptive to convergence later after the
 * instability of the early generations has passed.
 */
class VarianceMutationProbability(private val stats: DescriptiveStatistics,
                                  private val minProbability: Float,
                                  val maxProbability: Float,
                                  private val cvThresholdPercent: Float) : MutationProbability {

    init {
        if (maxProbability < minProbability) throw IllegalArgumentException("maxProbability must be more that minProbability")
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
        return minProbability + (maxProbability - minProbability) * (1 - cvPercent / cvThresholdPercent)
    }
}
