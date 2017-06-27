package com.petertackage.geneticsound

interface FitnessFunction {

    fun compare(target: ShortArray, proposed: ShortArray): Long
}