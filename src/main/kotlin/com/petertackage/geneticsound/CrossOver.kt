package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import com.petertackage.geneticsound.genetics.Pool

interface CrossOver {
    fun perform(pair: Pair<Individual<Clip>, Individual<Clip>>, mutator: Mutator, mutationProbability: Float, pool: Pool): Individual<Clip>
}