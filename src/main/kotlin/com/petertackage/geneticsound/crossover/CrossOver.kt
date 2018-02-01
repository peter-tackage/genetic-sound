package com.petertackage.geneticsound.crossover

import com.petertackage.geneticsound.Mutator
import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool

interface CrossOver {
    fun perform(pair: Pair<Individual<Clip>, Individual<Clip>>, mutator: Mutator, mutationProbability: Float, pool: Pool): Individual<Clip>
}