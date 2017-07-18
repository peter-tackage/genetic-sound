package com.petertackage.geneticsound

fun <T> measure(tag: String, func: () -> T): T {
    val start: Long = System.currentTimeMillis()
    val t = func()
    val diff: Long = System.currentTimeMillis() - start
    println("$tag took $diff ms")
    return t
}

fun measure(tag: String, action: () -> Unit): Long {
    val start: Long = System.currentTimeMillis()
    action()
    val diff: Long = System.currentTimeMillis() - start
    return diff
}