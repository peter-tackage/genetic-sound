package com.petertackage.geneticsound

class DiffFitnessFunction : FitnessFunction {
    override fun compare(target: ShortArray, proposed: ShortArray): Double {
        return proposed.mapIndexed { index, proposedFrame -> calculateDiff(target[index], proposedFrame).toLong() }
                .sum()
                .toDouble()
                .let { Math.min(it, Double.MAX_VALUE) }

    }

    private fun calculateDiff(targetFrame: Short, proposedFrame: Short): Int {
        // Need to use Int to as diff of Short.MAX_VALUE and Short.MIN_VALUE can't be represented in Short.
        return Math.abs(targetFrame - proposedFrame)
    }

}