package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual
import java.util.*

class Selector {

    private val random = Random()

    fun select(population: List<Individual<Clip>>): Individual<Clip> {
        // Pick an individual randomly from the upper fitness part of the population
        val bound = population.size * 0.25F
        val index = random.nextInt(bound.toInt())
        return population[index]
    }
}