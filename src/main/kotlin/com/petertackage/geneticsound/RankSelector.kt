package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import java.util.*

class RankSelector(val bias: Double = 1.0) : Selector {

    private val random = Random()

    override fun select(population: List<Individual<Clip>>): Individual<Clip> {
        val lastIndex: Int = (population.lastIndex * bias).toInt()
        val sum = lastIndex.cumulative()
        val roll = random.nextInt(sum)

        for (i in (0..lastIndex)) {
            if (roll <= i.cumulative()) return population[lastIndex - i]
        }

        throw IllegalStateException("You don't know how to program: $sum $roll")
    }

    private fun Int.cumulative() = (this * (this + 1)) / 2
}