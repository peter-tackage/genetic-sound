package com.petertackage.geneticsound.fitness

interface FitnessFunction {

    fun compare(target: ShortArray, proposed: ShortArray): Long
}