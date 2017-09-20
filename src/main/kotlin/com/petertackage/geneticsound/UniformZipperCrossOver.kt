package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import java.util.*

class UniformZipperCrossOver : CrossOver {

    private val random = Random()

    override fun perform(pair: Pair<Individual<Clip>, Individual<Clip>>, mutator: Mutator, mutationProbability: Float, pool: Pool): Individual<Clip> {
        return (pair.first.dna.indices).map { i ->
            // Either take from first or second parent depending on dice roll
            random.nextDouble()
                    .let { if (it > 0.5) pair.first.dna[i] else pair.second.dna[i] }
                    .let { mutator.mutate(it, mutationProbability, pool) }
        }.let { Individual(it) }
    }
}

