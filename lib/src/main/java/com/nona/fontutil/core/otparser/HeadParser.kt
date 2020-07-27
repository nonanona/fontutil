package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.*
import java.nio.ByteBuffer

data class Head(
    val unitsPerEm: Int,
    val xMin: Int,
    val xMax: Int,
    val yMin: Int,
    val yMax: Int,
    val indexToLocFormat: Int
)

object HeadParser {

    fun getHead(buffer: ByteBuffer, headOffset: Long): Head {
        buffer.position(headOffset)
        checkFormat(buffer.uint16() == 1) { "major version must be 1" } // majorVersion
        checkFormat(buffer.uint16() == 0) { "minor version must be 0" } // minorVersion
        buffer.uint32()  // ignore fontRevision
        buffer.uint32()  // ignore checkSumAdjustment
        checkFormat(buffer.uint32() == 0x5F0F3CF5L) { "invalid magic number"} // magic number
        buffer.uint16()  // ignore flags
        val unitsPerEm = buffer.uint16() // unitsPerEm
        buffer.int64() // ignore created
        buffer.int64() // ignore modified
        val xMin = buffer.int16()
        val yMin = buffer.int16()
        val xMax = buffer.int16()
        val yMax = buffer.int16()
        buffer.uint16() // ignore macStyle
        buffer.uint16() // ignore lowestRecPPEM
        buffer.int16() // ignore fontDirectionHint
        val indexToLocFormat = buffer.int16() // indexToLocFormat
        checkFormat(buffer.int16() == 0) { "glyphDataFormat must be 0" }
        return Head(
            unitsPerEm = unitsPerEm,
            xMin = xMin,
            xMax = xMax,
            yMin = yMin,
            yMax = yMax,
            indexToLocFormat = indexToLocFormat
        )
    }
}