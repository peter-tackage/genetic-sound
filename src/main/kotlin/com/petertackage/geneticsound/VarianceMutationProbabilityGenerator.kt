package com.petertackage.geneticsound

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class VarianceMutationProbabilityGenerator(val stats: DescriptiveStatistics,
                                           val baseProbability: Float,
                                           maxProbability: Float,
                                           val cvThresholdPercent: Float) : MutationProbabilityGenerator {

    private val deltaProbability = maxProbability - baseProbability

    init {
        if (maxProbability > baseProbability) throw IllegalArgumentException("maxProbability must be more that baseProbability")
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