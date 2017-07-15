package com.petertackage.geneticsound.genetics

import java.util.*

class Pool(val context: com.petertackage.geneticsound.Context) {

    private val random = Random()

    fun newPopulation(): List<Individual<Clip>> {
        val population = mutableListOf<Individual<Clip>>()
        (1..context.populationCount).forEach {
            population.add(newIndividual())
        }
        return population
    }

    fun newIndividual(): Individual<Clip> {
        val genes = mutableListOf<Clip>()
        (1..context.geneCount).forEach {
            genes.add(newClip())
        }
        return Individual<Clip>(genes)
    }

    fun newClip(): Clip {
        val clipIndex = random.nextInt(context.supportedClipTypes.size)
        return when (context.supportedClipTypes.get(clipIndex)) {
            ClipType.SINUSOID -> newSinusoidClip()
            else -> newSinusoidClip() // TODO Add Clip types later
        }
    }

    private fun newSinusoidClip(): Sinusoid {
        return Sinusoid(randomFrameRange(), context.frameRate, randomPeakAmplitude(), 0, randomFrequency())
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

    fun randomFrequency(): Float {
        // Max frequency produced is limited by Nyquist frequency
        val octave = random.nextInt(4)
        val note = Note.values()[random.nextInt(Note.values().lastIndex)]
        return (note.frequency * Math.pow(2.0, octave.toDouble())).toFloat()
    }

}