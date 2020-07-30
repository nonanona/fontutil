package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.int16
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import java.nio.ByteBuffer

object HmtxParser {
    fun getMetrix(
        buffer: ByteBuffer,
        hmtxOffset: Long,
        numberOfHMetrix: Int,
        glyphId: Int
    ): Pair<Int, Int> {
        buffer.position(hmtxOffset)
        if (glyphId < numberOfHMetrix) {
            buffer.position(hmtxOffset + 4 /* record size */ * glyphId)
            val advance = buffer.uint16()
            val lsb = buffer.int16()
            return Pair(advance, lsb)
        } else {
            buffer.position(hmtxOffset + 4 * numberOfHMetrix + 2 * (glyphId - numberOfHMetrix))
            val lsb = buffer.int16()
            return Pair(0, lsb)
        }
    }
}