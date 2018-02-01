package com.petertackage.geneticsound.mutation

import com.petertackage.geneticsound.coefficientOfVariance
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
                                  val maxProbability: Float) : MutationProbability {

    init {
        if (maxProbability < minProbability) throw IllegalArgumentException("maxProbability must be more that minProbability")
    }

    override fun next(): Float {
        // Smaller CV means less variance, therefore should increase the mutation rate.
        val cv: Float = stats.coefficientOfVariance().toFloat()  // 0 .. 100
        val probability = scaledProbability(cv)
        if (probability < minProbability || probability > maxProbability) throw IllegalStateException("Bad prob: $probability")
        return probability
    }

    private fun scaledProbability(cv: Float): Float {
        // Low CV, is low variance, so should equate to higher mutation rate. (approaching max)
        // High CV is high variance, so should equate to lower mutation rate (approaching min)
        return minProbability + (maxProbability - minProbability) * (1 - cv / 100f)
    }
}
