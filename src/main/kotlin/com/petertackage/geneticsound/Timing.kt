package com.petertackage.geneticsound

fun <T> measure(tag: String, func: () -> T): T {
    val start = System.currentTimeMillis()
    val t = func()
    val diff: Long = System.currentTimeMillis() - start
    println("$tag took $diff ms")
    return t
}
