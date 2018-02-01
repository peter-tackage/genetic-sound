package com.petertackage.geneticsound.genetics

import com.petertackage.geneticsound.Context
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

class Pool(val context: Context) {

    private val random = Random()

    fun newPopulation(): List<Individual<Clip>> {
        val deferred = (1..context.populationCount)
                .map { async(CommonPool) { newIndividual() } }

        return runBlocking { deferred.map { it.await() } }
    }

    fun newIndividual(): Individual<Clip> {
        val genes = mutableListOf<Clip>()
        (1..context.geneCount).forEach {
            genes.add(newClip())
        }
        return Individual(genes)
    }

    private fun newClip(): Clip {
        return Clip(randomFrameRange(), context.frameRate, randomPeakAmplitude(), randomFrequency(), randomWaveformType())
    }

    fun randomFrameRange(): IntRange {
        // Random range inside 0..context.targetFrameCount
        val lastPos = context.targetFrameCount - 1
        val start = random.nextInt(lastPos)
        val end = start + random.nextInt(lastPos - start)
        return IntRange(start, end)
    }

    fun randomPeakAmplitude(): Short {
        // Limit max to Short.MAX_VALUE
        return random.nextInt(Short.MAX_VALUE.toInt()).toShort()
    }

    // TODO This could be split up into different mutators
    fun randomFrequency(): Float {
        // Max frequency produced should be limited by Nyquist frequency (1/2 sample rate)
        // That is not strictly enforced here.
        val note = Note.values()[random.nextInt(Note.values().lastIndex)]

        // Flip a coin
        if (random.nextBoolean()) {
            val octave = random.nextInt(6) // G# with 8th Octave to 13 kHz.
            return (note.frequency * Math.pow(2.0, octave.toDouble())).toFloat()
        } else {
            // G# with 351 harmonic is ~18 kHz.
            val harmonic = random.nextInt(100) + 1
            return (note.frequency * harmonic)
        }
    }

    fun randomWaveformType(): WaveformType {
        val clipIndex = random.nextInt(context.supportedClipTypes.size)
        return context.supportedClipTypes.get(clipIndex)
    }

}
