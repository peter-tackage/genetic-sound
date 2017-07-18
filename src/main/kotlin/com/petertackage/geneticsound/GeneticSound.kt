package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.ClipType
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem


fun main(args: Array<String>) {
    GeneticSound(filename = args[0],
            populationCount = 100,
            geneCount = 100,
            supportedClipTypes = arrayOf(ClipType.SINUSOID),
            mutationProbability = 0.01F,
            fitnessFunction = AmplitudeDiffFitnessFunction(),
            selector = RankSelector(bias = 0.4),
            mutator = Mutator(),
            crossOver = ZipperCrossOver())
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
        var population: List<Individual<Clip>> = pool.newPopulation()
        var generation = 0
        var allTimeBest = Long.MAX_VALUE;

        do {

            val audioCanvas: ShortArray = ShortArray(audioFileFormat.frameLength).apply { fill(0) }

            // Assigns the fitness to each individual
            val duration = measure("Assign and Render ") {
                renderAndAssignFitness(targetShortArray, audioCanvas, population)
            }

            val best = population.sortedBy { it.fitness }.first().fitness
            if (best < allTimeBest) allTimeBest = best

            // Average fitness, SD, delta
            val populationFitness: DoubleArray = population.map { it.fitness.toDouble() }.toDoubleArray()
            println("$generation ${best} ${allTimeBest} ${populationFitness.avg()} ${populationFitness.sd()} $duration")

            writeToFile(audioCanvas, audioFileFormat)

            // Change the population
            population = buildNextGeneration(population, pool)
            generation++

        } while (true)

    }

    private fun writeToFile(audioCanvas: ShortArray, audioFileFormat: AudioFileFormat) {
        val buffer = ByteBuffer.allocate(audioCanvas.size * 2)
        buffer.asShortBuffer().put(audioCanvas)
        val bytes = buffer.array()
        writeToFile(audioFileFormat, bytes)
    }

    private fun writeToFile(audioFileFormat: AudioFileFormat, bytes: ByteArray) {
        val outputAudioFormat = AudioFormat(
                audioFileFormat.format.frameRate,
                audioFileFormat.format.sampleSizeInBits,
                1,
                true,
                true)
        val byteArrayInputStream = ByteArrayInputStream(bytes)

        val audioInputStream = AudioInputStream(
                byteArrayInputStream,
                outputAudioFormat,
                (bytes.size / 2).toLong())

        val file = File(filename.split(".").first() + "-evolved.wav")

        AudioSystem.write(
                audioInputStream,
                AudioFileFormat.Type.WAVE,
                file)
    }

    private fun buildNextGeneration(population: List<Individual<Clip>>, pool: Pool): List<Individual<Clip>> {
        return population.map { retainOrReplace(it, population.sortedBy { it.fitness }, pool) }
    }

    private fun retainOrReplace(individual: Individual<Clip>, populationByFitness: List<Individual<Clip>>, pool: Pool): Individual<Clip> {
        return if (isElite(individual, populationByFitness)) individual // elitism
        else {
            val one = selector.select(populationByFitness)
            val two = selector.select(populationByFitness) // hmm could selector same as `one`. weird.
            crossOver.perform(Pair(one, two), mutator, mutationProbability, pool)
        }
    }

    private fun renderAndAssignFitness(target: ShortArray,
                                       audioCanvas: ShortArray,
                                       population: List<Individual<Clip>>) {
        population.forEach { individual ->
            expressIndividual(audioCanvas, individual)
            individual.fitness = fitnessFunction.compare(target, audioCanvas)
        }
    }

    private fun isElite(individual: Individual<Clip>, population: List<Individual<Clip>>): Boolean {
        val avgFitness = population.map { it.fitness }.average()
        // Lower fitness is better!!!!
        return individual.fitness < avgFitness
    }

    private fun expressIndividual(audioCanvas: ShortArray, individual: Individual<Clip>) {
        individual.dna.forEach { clip ->
            clip.waveform().forEachIndexed { frameIndex, clipShort ->
                val audioCanvasIndex = clip.frameRange.start + frameIndex
                val merged: Short = mergeAudio(audioCanvas, audioCanvasIndex, clipShort)
                audioCanvas[audioCanvasIndex] = merged // update audio canvas
            }
        }
    }

    private fun mergeAudio(audioCanvas: ShortArray, audioCanvasIndex: Int, clipShort: Short): Short {
        // Average of existing merged values
        return ((audioCanvas[audioCanvasIndex].toInt() + clipShort.toInt()) / 2).toShort() // use Int to give 32 bits, which is 16 bits more headroom than Short
    }

    private fun DoubleArray.sd(): Double {
        return DescriptiveStatistics(this).standardDeviation
    }

    private fun DoubleArray.avg(): Double {
        return DescriptiveStatistics(this).mean
    }

}