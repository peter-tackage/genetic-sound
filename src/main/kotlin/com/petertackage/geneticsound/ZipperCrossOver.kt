package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import java.util.*

class ZipperCrossOver : CrossOver {

    private val random = Random()

    override fun perform(pair: Pair<Individual<Clip>, Individual<Clip>>, mutator: Mutator, mutationProbability: Float, pool: Pool): Individual<Clip> {
        return (pair.first.dna.indices).map { i ->
            // Either take from first or second parent
            // Copy the genes so that mutations don't affect parent!
            if (random.nextDouble() > 0.5) {
                mutator.mutate(pair.first.dna[i], mutationProbability, pool)
            } else {
                mutator.mutate(pair.second.dna[i], mutationProbability, pool)
            }
        }.let { Individual(it) }
    }
}

