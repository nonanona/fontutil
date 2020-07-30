package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.int16
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import java.nio.ByteBuffer

data class HheaMetrix(
    val ascent: Int,
    val descent: Int,
    val lineGap: Int,
    val numberOfHMetrics: Int
)

object HheaParser {
    fun getMetrix(buffer: ByteBuffer, hheaOffset: Long): HheaMetrix {
        buffer.position(hheaOffset)
        checkFormat(buffer.uint16() == 1) { "majorVersion must be 1"}
        checkFormat(buffer.uint16() == 0) { "majorVersion must be 0"}
        val ascent = buffer.int16()
        val descent = buffer.int16()
        val lineGap = buffer.int16()
        buffer.uint16() // skip advanceWidthMax
        buffer.int16() // skip minLeftSideBearing
        buffer.int16() // skip minRightSideBearing
        buffer.int16() // skip xMaxExtent
        buffer.int16() // careSlopeRise
        buffer.int16() // caretSlopeRun
        buffer.int16() // caretOffset
        buffer.int16() // reserved
        buffer.int16() // reserved
        buffer.int16() // reserved
        buffer.int16() // reserved
        buffer.int16() // metrixDataFormat
        val numberOfHMetrics = buffer.uint16()

        return HheaMetrix(
            ascent = ascent,
            descent = descent,
            lineGap = lineGap,
            numberOfHMetrics = numberOfHMetrics
        )

    }
}