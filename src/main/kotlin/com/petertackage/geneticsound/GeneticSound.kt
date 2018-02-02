package com.petertackage.geneticsound

import com.petertackage.geneticsound.crossover.CrossOver
import com.petertackage.geneticsound.crossover.UniformZipperCrossOver
import com.petertackage.geneticsound.fitness.AmplitudeDiffFitnessFunction
import com.petertackage.geneticsound.fitness.FitnessFunction
import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import com.petertackage.geneticsound.genetics.WaveformType
import com.petertackage.geneticsound.mutation.MutationProbability
import com.petertackage.geneticsound.mutation.VarianceMutationProbability
import com.petertackage.geneticsound.selector.RankSelector
import com.petertackage.geneticsound.selector.Selector
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    GeneticSound(filename = args[0],
            populationCount = 100,
            geneCount = 10,
            supportedClipTypes = arrayOf(WaveformType.SINUSOID, WaveformType.SQUARE, WaveformType.SAW),
            fitnessFunction = AmplitudeDiffFitnessFunction(),
            selector = RankSelector(bias = 0.4),
            mutator = Mutator(),
            crossOver = UniformZipperCrossOver())
            .run()
}

class GeneticSound(val filename: String,
                   val geneCount: Int,
                   val populationCount: Int,
                   val supportedClipTypes: Array<WaveformType>,
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
        // PCM_SIGNED is LPCM, which makes uniformly distributed sample volume generation simpler.

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
        val targetShortArray = ShortArray(audioFileFormat.frameLength)
        ByteBuffer.wrap(audioBytes)
                .order(if (audioFileFormat.format.isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(targetShortArray)

        val pool = Pool(Context(audioFileFormat.frameLength,
                audioFileFormat.format.frameRate,
                geneCount,
                populationCount,
                supportedClipTypes))

        // New random population
        var population: List<Individual<Clip>> = measure("Population creation") { pool.newPopulation() }
        var generation = 0

        do {

            // Assigns the fitness to each individual
            val duration = measureTimeMillis {
                renderAndAssignFitness(targetShortArray, population)
            }

            // Average fitness, SD, delta
            val fitnessStats = population
                    .map { it.fitness.toDouble() }
                    .toDoubleArray()
                    .let { DescriptiveStatistics(it) }

            val best = fitnessStats.min
            val worst = fitnessStats.max

            // Logging
            println("gen: $generation best: ${best} worst: ${worst} mean: ${fitnessStats.mean} sd: ${fitnessStats.standardDeviation} cv:${fitnessStats.coefficientOfVariance()} time: $duration")

            // Render the best one to file
            val sortedByFitness = population.sortedBy { it.fitness }

            newAudioCanvas(audioFileFormat.frameLength).apply {
                val fittest = sortedByFitness.first()
                expressIndividual(this, fittest)
                writeToFile(this, audioFileFormat, "fittest")
            }

            newAudioCanvas(audioFileFormat.frameLength).apply {
                val least = sortedByFitness.last()
                expressIndividual(this, least)
                writeToFile(this, audioFileFormat, "least")
            }

            // Build the next generation of the population
            population = buildNextGeneration(population, fitnessStats, pool)
            generation++

        } while (true)

    }

    private fun newAudioCanvas(frameLength: Int) =
            ShortArray(frameLength).apply { fill(0) }

    private fun writeToFile(audioCanvas: ShortArray, audioFileFormat: AudioFileFormat, tag: String) {
        val buffer = ByteBuffer.allocate(audioCanvas.size * 2)
        buffer.order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(audioCanvas) // ByteBuffer is big-endian by default, but be explicit.
        val bytes = buffer.array()
        writeToFile(audioFileFormat, bytes, tag)
    }

    private fun writeToFile(audioFileFormat: AudioFileFormat, bytes: ByteArray, tag: String) {
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

        val file = File(filename.split(".").first() + "-evolved-$tag.wav")

        AudioSystem.write(
                audioInputStream,
                AudioFileFormat.Type.WAVE,
                file)
    }

    private fun buildNextGeneration(population: List<Individual<Clip>>, fitnessStats: DescriptiveStatistics, pool: Pool): List<Individual<Clip>> {
        val mutationProbability = VarianceMutationProbability(fitnessStats,
                minProbability = 0.01F,
                maxProbability = 0.10F)
        val populationByFitness = population.sortedBy { it.fitness }
        return runBlocking {
            // Fitness is sorted in descending order - fittest items are first.
            population.map { async(CommonPool) { retainOrReplace(it, populationByFitness, mutationProbability, pool) } }
                    .map { it.await() }
        }
    }

    private fun retainOrReplace(individual: Individual<Clip>, populationByFitness: List<Individual<Clip>>, mutationProbability: MutationProbability, pool: Pool): Individual<Clip> {
        return if (isElite(individual, populationByFitness)) individual // elitism
        else {
            val one = selector.select(populationByFitness)
            val two = selector.select(populationByFitness) // hmm could selector same as `one`. weird.
            crossOver.perform(Pair(one, two), mutator, mutationProbability.next(), pool)
        }
    }

    private fun renderAndAssignFitness(target: ShortArray,
                                       population: List<Individual<Clip>>) {
        // ####### Note: expressing take ~30ms, comparison ~3ms. #######
        population.forEach { individual ->
            // New canvas for each individual, express then evaluate.
            val audioCanvas = newAudioCanvas(target.size)
            expressIndividual(audioCanvas, individual)
            individual.fitness = fitnessFunction.compare(target, audioCanvas)
        }
    }

    private fun isElite(individual: Individual<Clip>, populationByFitness: List<Individual<Clip>>): Boolean {
        val avgFitness = populationByFitness.map { it.fitness }.average()
        // Lower fitness is better!!!!
        return individual.fitness < avgFitness
    }

    // Mutates the audio canvas.
    private fun expressIndividual(audioCanvas: ShortArray, individual: Individual<Clip>): ShortArray {
        individual.dna.forEach { clip ->
            clip.waveform().forEachIndexed { frameOffsetIndex, clipSample ->
                val audioCanvasIndex = clip.frameRange.start + frameOffsetIndex
                val mergedFrame = mergeAudioFrame(audioCanvas.get(audioCanvasIndex), clipSample)
                audioCanvas[audioCanvasIndex] = mergedFrame
            }
        }
        return audioCanvas
    }

    private fun mergeAudioFrame(audioCanvasSample: Short, clipSample: Short): Short {
        // Average of existing merged values
        // use Int before division to give 32 bits, which is 16 bits more headroom than Short
        return ((audioCanvasSample + clipSample.toInt()) / 2).toShort()
    }

}
