package com.petertackage.geneticsound.fitness

import com.petertackage.geneticsound.sumExactOrClip

class AmplitudeDiffFitnessFunction : FitnessFunction {

    // Represent the diff in Double/Long (64bits) to allow for the greatest resolution when determining fitness
    // If we clip the diff at Short.MAX/MIN or Int.MAX/MIN then we won't see improvements in the algorithm when
    // the population is unfit.
    override fun compare(target: ShortArray, proposed: ShortArray): Long {
        return proposed.mapIndexed { index, proposedFrame -> calculateDiff(target[index], proposedFrame).toLong() }
                .sumExactOrClip()
    }

    private fun calculateDiff(targetFrame: Short, proposedFrame: Short): Int {
        // Need to use Int to as diff of Short.MAX_VALUE and Short.MIN_VALUE can't be represented in Short.
        return Math.abs(targetFrame - proposedFrame)
    }

}
