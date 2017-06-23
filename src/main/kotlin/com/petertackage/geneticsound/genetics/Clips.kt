package com.petertackage.geneticsound.genetics

data class Individual<T : Any>(val dna: List<T>,
                               var fitnesss: Double = Double.MAX_VALUE)

abstract class Clip(var frameRange: IntRange,
                    val frameRate: Float,
                    var peakAmplitude: Short) {

    abstract fun waveform(): ShortArray
}

class Noise(frameRange: IntRange,
            frameRate: Float,
            peakAmplitude: Short) : Clip(frameRange, frameRate, peakAmplitude) {
    override fun waveform(): ShortArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

abstract class Periodic(frameRange: IntRange,
                        frameRate: Float,
                        peakAmplitude: Short,
                        val phaseOffset: Int = 0,
                        var frequency: Float) : Clip(frameRange, frameRate, peakAmplitude)

class Triangle(frameRange: IntRange,
               frameRate: Float,
               peakAmplitude: Short,
               phaseOffset: Int,
               frequency: Float) : Periodic(frameRange, frameRate, peakAmplitude, phaseOffset, frequency) {
    override fun waveform(): ShortArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

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