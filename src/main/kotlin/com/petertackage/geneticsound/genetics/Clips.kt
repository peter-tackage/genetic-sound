package com.petertackage.geneticsound.genetics

data class Individual<T : Any>(val dna: List<T>,
                               var fitness: Long = Long.MAX_VALUE)


enum class Note(val displayName: String, val frequency: Float) {
    A("A", 220.00F),
    A_SHARP("A#", 233.082F),
    B("B", 246.942F),
    C("C", 261.626F),
    C_SHARP("C#", 277.183F),
    D("D", 293.665F),
    D_SHARP("D#", 311.127F),
    E("E", 329.628F),
    F("F", 349.228F),
    F_SHARP("F#", 369.994F),
    G("G", 391.995F),
    G_SHARP("G#", 415.305F)
}

abstract class Clip(var frameRange: IntRange,
                    val frameRate: Float,
                    var peakAmplitude: Short) {

    abstract fun waveform(): ShortArray
}

abstract class Periodic(frameRange: IntRange,
                        frameRate: Float,
                        peakAmplitude: Short,
                        val phaseOffset: Int = 0,
                        var frequency: Float) : Clip(frameRange, frameRate, peakAmplitude)

class Sinusoid(frameRange: IntRange,
               frameRate: Float,
               peakAmplitude: Short,
               phaseOffset: Int,
               frequency: Float) : Periodic(frameRange, frameRate, peakAmplitude, phaseOffset, frequency) {

    private val waveform: ShortArray

    init {
        waveform = frameRange
                .map {
                    // start from 0
                    val radians = (it - frameRange.start) / frameRate * frequency * 2.0 * Math.PI
                    (peakAmplitude * Math.sin(radians)).toShort()
                }.toShortArray()
    }

    override fun waveform(): ShortArray {
        return waveform
    }

    fun copy(frameRange: IntRange = this.frameRange,
             frameRate: Float = this.frameRate,
             peakAmplitude: Short = this.peakAmplitude,
             phaseOffset: Int = this.phaseOffset,
             frequency: Float = this.frequency): Sinusoid = Sinusoid(frameRange, frameRate, peakAmplitude, phaseOffset, frequency)

}