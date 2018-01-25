package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import java.util.*

/**
 * A Selector which selects an individual with a linearly weighted preference depending on the
 * input population's position ranking.
 *
 * The bias parameter controls which percentage of the population should be considered for selection.
 * By default the entire population is considered (bias = 1).
 *
 * Note: it assumes the input population is sorted in descending order of fitness: the fittest items are first.
 */
class RankSelector(val bias: Double = 1.0) : Selector {

    private val random = Random()

    init {
        if (bias > 1 || bias < 0) throw IllegalArgumentException("bias must be: 0.0 <= bias <= 1.0")
    }

    override fun select(population: List<Individual<Clip>>): Individual<Clip> {
        val lastIndex: Int = (population.lastIndex * bias).toInt()
        val sum = lastIndex.cumulative()
        val roll = random.nextInt(sum)

        // Find the individual corresponding to the roll.
        for (i in (0..lastIndex)) {
            if (roll <= i.cumulative()) return population[lastIndex - i]
        }

        throw IllegalStateException("You don't know how to program: $sum $roll")
    }

    private fun Int.cumulative() = (this * (this + 1)) / 2
}
