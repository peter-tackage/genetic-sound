package com.petertackage.geneticsound.mutation

class ConstantMutationProbability(val probability: Float) : MutationProbability {
    override fun next(): Float {
        return probability
    }
}
