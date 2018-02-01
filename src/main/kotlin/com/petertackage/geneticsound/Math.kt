package com.petertackage.geneticsound

fun List<Long>.sumExact(): Long {
    var sum = 0L
    for (element in this) {
        try {
            sum = Math.addExact(sum, element)
        } catch (exp: ArithmeticException) {
            sum = Long.MAX_VALUE
        }
    }
    return sum
}