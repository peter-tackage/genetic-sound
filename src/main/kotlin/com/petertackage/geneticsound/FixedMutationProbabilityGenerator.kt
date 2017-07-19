package com.petertackage.geneticsound

class FixedMutationProbabilityGenerator(val probability: Float) : MutationProbabilityGenerator {
    override fun next(): Float {
        return probability
    }
}