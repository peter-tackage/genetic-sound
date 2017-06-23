package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.ClipType
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem


fun main(args: Array<String>) {
    GeneticSound(filename = "/Users/ptac/code/genetic-sound/185347__lemoncreme__symphony-sounds_MONO.wav",
            geneCount = 50,
            populationCount = 20,
            supportedClipTypes = arrayOf(ClipType.SINUSOID),
            mutationProbability = 0.01F,
            fitnessFunction = DiffFitnessFunction(),
            selector = Selector(),
            mutator = Mutator(),
            crossOver = CrossOver())
            .run()
}

class GeneticSound(val filename: String,
                   val geneCount: Int,
                   val populationCount: Int,
                   val supportedClipTypes: Array<ClipType>,
                   val mutationProbability: Float,
                   val fitnessFunction: FitnessFunction,
                   val selector: Selector,
                   val crossOver: CrossOver,
                   val mutator: Mutator) {

    fun run() {
        val audioFileFormat = AudioSystem.getAudioFileFormat(File(filename))

        audioFileFormat.type
                .takeIf { it != AudioFileFormat.Type.WAVE }
                ?.let { throw IllegalArgumentException("Only WAVE audio type is supported, found: $it") }

        audioFileFormat.format.channels
                .takeIf { it != 1 }
                ?.let { throw IllegalArgumentException("Only a single audio channel (mono) is supported, found: $it") }

        audioFileFormat.format.encoding
                .takeIf { it != AudioFormat.Encoding.PCM_SIGNED }
                ?.let { throw IllegalArgumentException("Only is PCM Signed encoding is supported, found: $it") }

        // PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian

        // A frame is a measure of length/size, so a file can by 120 frames long and each
        // frame contains 4 bytes
        println("Format: ${audioFileFormat.format}")

        // 756197 frames = 756197 *  audioFileFormat.format.frameSize bytes
        println("FrameLength: ${audioFileFormat.frameLength}")

        val fileIn = File(filename)
        val audioBytes = AudioSystem.getAudioInputStream(fileIn).readBytes()

        println("Read ${audioBytes.size} audio bytes")

        // We have 16 bit dynamic range, so need to use 32 bits to sum them

        // Synthesizing
        // http://www.developer.com/java/other/article.php/2226701
        // http://jsresources.org/examples/

        // According to http://soundfile.sapp.org/doc/WaveFormat/
        // 8-bit samples are stored as unsigned bytes, ranging from 0 to 255
        // 16-bit samples are stored as 2's-complement signed integers, ranging from -32768 to 32767.
        // Use Short because two bytes per channel per frame to read the data
        // The short data type is a 16-bit signed two's complement integer
        // But: Using Short (16 bit sample size) doesn't get us any headroom for operations.

        // TODO Make support other frame sizes (dynamic ranges), currently only supports 16 bit.
        val targetShortArray: ShortArray = ShortArray(audioFileFormat.frameLength)
        ByteBuffer.wrap(audioBytes)
                .order(if (audioFileFormat.format.isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(targetShortArray)

        val pool = Pool(Context(audioFileFormat.frameLength,
                audioFileFormat.format.frameRate,
                audioFileFormat.format.encoding,
                geneCount,
                populationCount,
                supportedClipTypes))

        // New random population
        var population: List<Individual<Clip>> = measure("Building population") { pool.newPopulation() }
        var fitness = Double.MAX_VALUE
        var generation = 0

        val audioCanvas: ShortArray = measure("initializing") {
            ShortArray(audioFileFormat.frameLength).apply { fill(0) }
        }

        do {
            population = measure("Evaluating fitness") { evaluateFitness(targetShortArray, audioCanvas, population) }
            println("${generation}, ${population.first().fitnesss}")

            // TODO Save the audioCanvas to file every nth time (perf. reasons)

            population = measure("Building next generation") {
                buildNextGeneration(population, pool) // mutates
            }
            generation++

        } while (fitness > 0)

    }

    private fun buildNextGeneration(population: List<Individual<Clip>>, pool: Pool): List<Individual<Clip>> {
        val nextGeneration = mutableListOf<Individual<Clip>>()

        // elitism
        nextGeneration.add(population.first())

        while (nextGeneration.size < population.size) {
            val one = selector.select(population)
            val two = selector.select(population) // hmm could selector same as `one`
            nextGeneration.add(crossOver.perform(Pair(one, two), mutator, mutationProbability, pool))
        }
        return nextGeneration
    }

    private fun evaluateFitness(target: ShortArray,
                                audioCanvas: ShortArray,
                                population: List<Individual<Clip>>): List<Individual<Clip>> {
        population.forEach { individual ->
            expressIndividual(audioCanvas, individual)
            individual.fitnesss = fitnessFunction.compare(target, audioCanvas)
        }
        return population.sortedBy { individual -> individual.fitnesss }
    }

    private fun expressIndividual(audioCanvas: ShortArray, individual: Individual<Clip>) {
        individual.dna.forEachIndexed { clipIndex, clip ->
            clip.waveform().forEachIndexed { frameIndex, clipShort ->
                val merged: Short = mergeAudio(audioCanvas, frameIndex, clipShort, clipIndex)
                audioCanvas[frameIndex] = merged // update audio canvas
            }
        }
    }

    private fun mergeAudio(audioCanvas: ShortArray, frameIndex: Int, clipShort: Short, clipIndex: Int): Short {
        val total: Int = (audioCanvas[frameIndex] + clipShort) // use Int to give 32 bits, which is 16 bits more headroom than Short
        return (total shr 16).toShort() // normalize the value back from 32 bits to 16 bits.
    }

}