package com.nona.fontutil.core.otparser

import android.util.Log
import com.nona.fontutil.base.*
import java.nio.ByteBuffer

object CMapParser {
    fun getGlyphIdFromCmapFormat4(fontBuffer: ByteBuffer, cmapOffset: Long, codePoint: Int): Int {
        fontBuffer.position(cmapOffset)

        fontBuffer.uint16() // ignore format
        fontBuffer.uint16() // ignore length
        fontBuffer.uint16() // ignore language
        val segCount = fontBuffer.uint16() / 2 // segCount is doubled
        fontBuffer.uint16() // ignore searchRange
        fontBuffer.uint16() // ignore entrySelector
        fontBuffer.uint16() // ignore rangeShift

        // From now, read as int16 array.
        val shortBuffer = fontBuffer.asShortBuffer()

        val endCodes = ShortArray(segCount)
        val startCodes = ShortArray(segCount)
        val idDeltas = ShortArray(segCount)
        val idRangeOffsets = ShortArray(segCount)

        shortBuffer.get(endCodes, 0, segCount)
        shortBuffer.uint16()  // ignore reservedPad
        shortBuffer.get(startCodes, 0, segCount)
        shortBuffer.get(idDeltas, 0, segCount)  // unused but for skipping
        shortBuffer.get(idRangeOffsets, 0, segCount)

        // TODO: binary search
        for (i in 0 until segCount - 1) {
            val start = startCodes[i].toUnsigned()
            val end = endCodes[i].toUnsigned()
            if (start <= codePoint && codePoint < end) {
                return if (idRangeOffsets[i] == 0.toShort()) {
                    codePoint + idDeltas[i]
                } else {
                    TODO("Not yet implemented")
                }
            }


        }
        return 0 // 0 is reserved for no-glyph ID.
    }

    fun getGlyphIdFromCmapFormat12(fontBuffer: ByteBuffer, cmapOffset: Long, codePoint: Int): Int {
        fontBuffer.position(cmapOffset)

        fontBuffer.uint16()  // ignore format since we already know it
        fontBuffer.uint16()  // ignore reserved
        fontBuffer.uint32()  // ignore length
        fontBuffer.uint32()  // ignore language
        val numGroups = fontBuffer.uint32()

        val sbsBuilder = SparseBitSet.Builder()
        // TODO: binary search
        for (i in 0 until numGroups) {
            val startCharCode = fontBuffer.uint32()
            val endCharCode = fontBuffer.uint32()
            val startGlyphId = fontBuffer.uint32()

            if (startCharCode <= codePoint && codePoint <= endCharCode) { // inc-inc
                return (startGlyphId + (codePoint - startCharCode)).toInt()
            }
        }
        return 0 // 0 is reserved for no-glyph ID
    }

    fun getPreferredCmapOffset(fontBuffer: ByteBuffer, cmapOffset: Long): Long {
        fontBuffer.position(cmapOffset)
        fontBuffer.uint16()  // ignore version
        val numTables = fontBuffer.uint16()

        var highestScore = Int.MAX_VALUE
        var highestTableOffset = 0L

        for (i in 0 until numTables) {
            val platformId = fontBuffer.uint16()
            val encodingId = fontBuffer.uint16()
            val offset = fontBuffer.uint32()

            val score = cmapTablePriority(platformId, encodingId)
            if (score == -1) continue  // Unsupported table
            if (score < highestScore) {
                highestScore = score
                highestTableOffset = offset
            }
        }

        return highestTableOffset
    }

    fun getGlyphId(buffer: ByteBuffer, cmapOffset: Long, codePoints: Int): Int {
        buffer.position(cmapOffset)
        val format = buffer.uint16()
        return when (format) {
            4 -> CMapParser.getGlyphIdFromCmapFormat4(buffer, cmapOffset, codePoints)
            12 -> CMapParser.getGlyphIdFromCmapFormat12(buffer, cmapOffset, codePoints)
            else -> 0
        }
    }


    // Copied from CmapCoverage.cpp in minikin.
    // Lower is higher priority.
    private fun cmapTablePriority(platformId: Int, encodingId: Int): Int =
        when {
            platformId == 3 && encodingId == 10 -> 0
            platformId == 0 && encodingId == 6 -> 1
            platformId == 0 && encodingId == 4 -> 2
            platformId == 3 && encodingId == 1 -> 3
            platformId == 0 && encodingId == 3 -> 4
            platformId == 0 && encodingId == 2 -> 5
            platformId == 0 && encodingId == 1 -> 6
            platformId == 0 && encodingId == 0 -> 7
            else -> -1
        }
}