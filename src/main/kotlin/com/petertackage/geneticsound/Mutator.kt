package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.*
import java.util.*

class Mutator {

    private val random = Random()

    fun mutate(gene: Clip, probability: Float, pool: Pool): Clip {
        if (random.nextFloat() > probability) return gene
        return when (gene) {
            is Sinusoid -> mutateSinusoid(gene, pool)
            is Square -> mutateSquare(gene, pool)
            is Saw -> mutateSaw(gene, pool)
            else -> throw IllegalArgumentException("Mutation not supported for: $gene")
        }
    }

    private fun mutateSinusoid(clip: Sinusoid, pool: Pool): Sinusoid {
        // Copy the genes so that mutations don't affect parent!
        return when (random.nextInt(3)) {
            0 -> clip.copy(frequency = pool.randomFrequency())
            1 -> clip.copy(frameRange = pool.randomFrameRange())
            2 -> clip.copy(peakAmplitude = pool.randomPeakAmplitude())
            else -> throw Exception("Got bad random")
        }
    }

    private fun mutateSquare(clip: Square, pool: Pool): Square {
        // Copy the genes so that mutations don't affect parent!
        return when (random.nextInt(3)) {
            0 -> clip.copy(frequency = pool.randomFrequency())
            1 -> clip.copy(frameRange = pool.randomFrameRange())
            2 -> clip.copy(peakAmplitude = pool.randomPeakAmplitude())
            else -> throw Exception("Got bad random")
        }
    }

    private fun mutateSaw(clip: Saw, pool: Pool): Saw {
        // Copy the genes so that mutations don't affect parent!
        return when (random.nextInt(3)) {
            0 -> clip.copy(frequency = pool.randomFrequency())
            1 -> clip.copy(frameRange = pool.randomFrameRange())
            2 -> clip.copy(peakAmplitude = pool.randomPeakAmplitude())
            else -> throw Exception("Got bad random")
        }
    }

}
