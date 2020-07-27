package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import com.nona.fontutil.base.uint32
import java.nio.ByteBuffer

data class MaxProfile(
    val numGlyph: Int
)

object MaxpParser {
    fun getMaxProfile(buffer: ByteBuffer, maxpOffset: Long): MaxProfile {
        buffer.position(maxpOffset)
        val version = buffer.uint32()
        val numGlyph = buffer.uint16()

        return MaxProfile(
            numGlyph = numGlyph
        )
    }
}