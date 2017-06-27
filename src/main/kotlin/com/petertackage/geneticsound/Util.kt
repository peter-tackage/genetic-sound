package com.petertackage.geneticsound

fun <T> measure(tag: String, func: () -> T): T {
    val start: Long = System.currentTimeMillis()
    val t = func()
    val diff: Long = System.currentTimeMillis() - start
    println("$tag took $diff ms")
    return t
}

fun Int.clipToShort(): Short {
    return if (this > Short.MAX_VALUE) {
        Short.MAX_VALUE
    } else if (this < Short.MIN_VALUE) {
        Short.MIN_VALUE
    } else {
        this.toShort()
    }
}

fun Long.clipToShort(): Short {
    return if (this > Short.MAX_VALUE) {
        Short.MAX_VALUE
    } else if (this < Short.MIN_VALUE) {
        Short.MIN_VALUE
    } else {
        this.toShort()
    }
}