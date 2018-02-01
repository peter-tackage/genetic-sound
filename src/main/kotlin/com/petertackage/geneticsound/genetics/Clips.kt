package com.petertackage.geneticsound.genetics

data class Individual<T : Any>(val dna: List<T>,
                               var fitness: Long = Long.MAX_VALUE)

enum class Note(val displayName: String, val frequency: Float) {
    A("A", 27.5f),
    A_SHARP("A#", 29.135f),
    B("B", 30.868f),
    C("C", 32.703f),
    C_SHARP("C#", 34.648f),
    D("D", 36.708f),
    D_SHARP("D#", 38.891f),
    E("E", 41.203f),
    F("F", 43.654f),
    F_SHARP("F#", 46.249f),
    G("G", 48.999f),
    G_SHARP("G#", 51.913f)
}

val TWO_PI = 2 * Math.PI;

abstract class Clip(val frameRange: IntRange,
                    val frameRate: Float,
                    val peakAmplitude: Short) {

    abstract fun waveform(): ShortArray
}

abstract class Periodic(frameRange: IntRange,
                        frameRate: Float,
                        peakAmplitude: Short,
                        val frequency: Float) : Clip(frameRange, frameRate, peakAmplitude)

class Sinusoid(frameRange: IntRange,
               frameRate: Float,
               peakAmplitude: Short,
               frequency: Float) : Periodic(frameRange, frameRate, peakAmplitude, frequency) {

    private val waveform: ShortArray

    init {
        waveform = frameRange
                .map {
                    // start from 0
                    val radians = (it - frameRange.start) / frameRate * frequency * TWO_PI
                    (peakAmplitude * Math.sin(radians)).toShort()
                }.toShortArray()
    }

    override fun waveform(): ShortArray {
        return waveform
    }

    fun copy(frameRange: IntRange = this.frameRange,
             frameRate: Float = this.frameRate,
             peakAmplitude: Short = this.peakAmplitude,
             frequency: Float = this.frequency): Sinusoid = Sinusoid(frameRange, frameRate, peakAmplitude, frequency)

}

class Square(frameRange: IntRange,
             frameRate: Float,
             peakAmplitude: Short,
             frequency: Float) : Periodic(frameRange, frameRate, peakAmplitude, frequency) {

    private val waveform: ShortArray

    init {
        waveform = frameRange
                .map {
                    val radians = (it - frameRange.start) / frameRate * frequency * TWO_PI
                    val remRads = radians.rem(TWO_PI)
                    if (remRads < Math.PI) peakAmplitude else (-peakAmplitude).toShort()
                }.toShortArray()
    }

    override fun waveform(): ShortArray {
        return waveform
    }

    fun copy(frameRange: IntRange = this.frameRange,
             frameRate: Float = this.frameRate,
             peakAmplitude: Short = this.peakAmplitude,
             frequency: Float = this.frequency): Square = Square(frameRange, frameRate, peakAmplitude, frequency)

}

class Saw(frameRange: IntRange,
          frameRate: Float,
          peakAmplitude: Short,
          frequency: Float) : Periodic(frameRange, frameRate, peakAmplitude, frequency) {

    private val waveform: ShortArray

    init {
        waveform = frameRange
                .map {
                    val period = (it - frameRange.start).rem(frameRate / frequency)
                    ((2 * peakAmplitude * frequency) * (period / frameRate) - peakAmplitude).toShort()
                }.toShortArray()
    }

    override fun waveform(): ShortArray {
        return waveform
    }

    fun copy(frameRange: IntRange = this.frameRange,
             frameRate: Float = this.frameRate,
             peakAmplitude: Short = this.peakAmplitude,
             frequency: Float = this.frequency): Saw = Saw(frameRange, frameRate, peakAmplitude, frequency)

}
