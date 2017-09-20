package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Pool
import com.petertackage.geneticsound.genetics.Sinusoid
import java.util.*

class Mutator {

    private val random = Random()

    fun mutate(gene: Clip, probability: Float, pool: Pool): Clip {
        if (random.nextFloat() > probability) return gene
        return when (gene) {
            is Sinusoid -> mutateSinusoid(gene, pool)
            else -> gene // TODO Add support for mutating other types
        }
    }

    private fun mutateSinusoid(gene: Sinusoid, pool: Pool): Clip {
        // Copy the genes so that mutations don't affect parent!
        return when (random.nextInt(3)) {
            0 -> gene.copy(frequency = pool.randomFrequency())
            1 -> gene.copy(frameRange = pool.randomFrameRange())
            2 -> gene.copy(peakAmplitude = pool.randomPeakAmplitude())
            else -> throw Exception("Got bad random")
        }
    }

}