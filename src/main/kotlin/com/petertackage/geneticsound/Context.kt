package com.petertackage.geneticsound

import com.petertackage.geneticsound.genetics.ClipType
import javax.sound.sampled.AudioFormat

class Context(val targetFrameCount: Int,
              val frameRate: Float,
              val encoding: AudioFormat.Encoding,
              val geneCount: Int,
              val populationCount: Int,
              val supportedClipTypes: Array<ClipType>)