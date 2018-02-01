package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Pool
import java.util.*

class Mutator {

    private val random = Random()

    fun mutate(gene: Clip, probability: Float, pool: Pool): Clip {
        return if (random.nextFloat() > probability) gene
        else mutate(gene, pool)
    }

    private fun mutate(clip: Clip, pool: Pool): Clip {
        // Copy the genes so that mutations don't affect parent!
        return when (random.nextInt(4)) {
            0 -> clip.copy(frequency = pool.randomFrequency())
            1 -> clip.copy(frameRange = pool.randomFrameRange())
            2 -> clip.copy(peakAmplitude = pool.randomPeakAmplitude())
            3 -> clip.copy(waveformType = pool.randomWaveformType())
            else -> throw Exception("Got bad random")
        }
    }

}
