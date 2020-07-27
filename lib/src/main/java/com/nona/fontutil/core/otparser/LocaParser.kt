package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import com.nona.fontutil.base.uint32
import java.lang.RuntimeException
import java.nio.ByteBuffer

object LocaParser {
    fun getGlyphOffset(
        buffer: ByteBuffer,
        locaOffset: Long,
        locaType: Int,
        maxGlyph: Int,
        glyphId: Int
    ): Long {
        buffer.position(locaOffset)
        if (locaType == 0) {
            return buffer.asShortBuffer().uint16(glyphId).toLong() * 2
        } else {
            return buffer.asIntBuffer().uint32(glyphId)
        }

    }
}