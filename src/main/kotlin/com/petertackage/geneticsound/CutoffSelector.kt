package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import java.util.*

/**
 * A Selector with a hard cutoff percentage. Only individuals within the cutoff are candidates for selection.
 * The completely eliminates lower fitness individuals from selection.
 *
 * Thereafter, all candidates have an equal chance of being selected.
 */
class CutoffSelector(val cutoff: Double = 0.25) : Selector {

    private val random = Random()

    init {
        if (cutoff > 1 || cutoff < 0) throw IllegalArgumentException("cutoff must be: 0.0 <= cutoff <= 1.0")
    }

    override fun select(population: List<Individual<Clip>>): Individual<Clip> {
        // Pick an individual randomly from the upper fitness part of the population
        val bound = Math.max(2, (population.size * cutoff).toInt())
        val index = random.nextInt(bound)
        return population[index]
    }
}
