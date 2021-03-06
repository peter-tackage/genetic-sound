package com.petertackage.geneticsound.selector

import com.petertackage.geneticsound.genetics.Clip
import com.petertackage.geneticsound.genetics.Individual

interface Selector {

    fun select(population: List<Individual<Clip>>): Individual<Clip>
}