package com.petertackage.geneticsound.genetics

import org.junit.Test
import java.util.*

class SawTest {

    @Test
    fun testsomething() {

        val saw = Saw(0..500, 1000f, 10, 20f)

        println(Arrays.toString(saw.waveform()))
    }

}