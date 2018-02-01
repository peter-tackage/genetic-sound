package com.petertackage.geneticsound

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

fun DescriptiveStatistics.coefficientOfVariance(): Double {
    return (this.standardDeviation / this.mean) * 100
}