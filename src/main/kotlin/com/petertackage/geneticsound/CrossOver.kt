package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool
import java.util.*

class CrossOver {

    private val random = Random()

    fun perform(pair: Pair<Individual<Clip>, Individual<Clip>>, mutator: Mutator, mutationProbability: Float, pool: Pool): Individual<Clip> {
        val genes = mutableListOf<Clip>()
        (pair.first.dna.indices).forEach { i ->
            // Either take from first or second parent
            // Copy the genes so that mutations don't affect parent!
            if (random.nextDouble() > .5) {
                genes.add(mutator.mutate(pair.first.dna[i], mutationProbability, pool))
            } else {
                genes.add(mutator.mutate(pair.second.dna[i], mutationProbability, pool))
            }
        }
        return Individual(genes)
    }
}