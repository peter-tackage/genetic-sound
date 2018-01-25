package com.petertackage.geneticsound

class ConstantMutationProbability(val probability: Float) : MutationProbability {
    override fun next(): Float {
        return probability
    }
}
