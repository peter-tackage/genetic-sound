package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.WaveformType

class Context(val targetFrameCount: Int,
              val frameRate: Float,
              val geneCount: Int,
              val populationCount: Int,
              val supportedClipTypes: Array<WaveformType>)