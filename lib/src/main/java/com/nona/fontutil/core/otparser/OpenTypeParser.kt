package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.SparseBitSet
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class FontStyle(val weight: Int, val italic: Boolean)

private data class FontTable(val tag: Long, val offset: Int, val length: Int)

private fun ByteBuffer.uint32(): Long = int.toLong() and 0xFFFF_FFFFL
private fun ByteBuffer.int32(): Int = int
private fun ByteBuffer.uint16(): Int = short.toInt() and 0xFFFF
private fun ByteBuffer.int16(): Int = short.toInt()
private fun ByteBuffer.uint8(): Int = get().toInt() and 0xFF
private fun ByteBuffer.int8(): Int = get().toInt()

private fun ByteBuffer.uint32(i: Int): Long = getInt(i).toLong() and 0xFFFF_FFFFL
private fun ByteBuffer.int32(i: Int): Int = getInt(i)
private fun ByteBuffer.uint16(i: Int): Int = getShort(i).toInt() and 0xFFFF
private fun ByteBuffer.int16(i: Int): Int = getShort(i).toInt()
private fun ByteBuffer.uint8(i: Int): Int = get(i).toInt() and 0xFF
private fun ByteBuffer.int8(i: Int): Int = get(i).toInt()

// We are not supporting over 2GB font files. Just cast to Int.
private fun ByteBuffer.position(i: Long) = position(i.toInt())

private const val SFNT_VERSION_1_0 = 0x0001_0000L
private const val SFNT_TAG_OTTO = 0x4F_54_54_4FL

private const val TAG_OS_2 = 0x4F_53_2F_32L
private const val TAG_cmap = 0x63_6D_61_70L

class OpenTypeParser(fontBuffer: ByteBuffer) {

    private val fontBuffer = fontBuffer.slice().apply { order(ByteOrder.BIG_ENDIAN) }

    private val tableMap: Map<Long, FontTable> by lazy(LazyThreadSafetyMode.NONE) {
        if (fontBuffer.position() != 0) throw IOException("Must parse file header first")
        val sfntVersion = fontBuffer.uint32()
        if (sfntVersion != SFNT_TAG_OTTO && sfntVersion != SFNT_VERSION_1_0) {
            throw IOException("sfntVersion is invalid ${sfntVersion}")
        }
        val numTables = fontBuffer.uint16()
        fontBuffer.uint16()  // ignore searchRange
        fontBuffer.uint16()  // ignore entrySelector
        fontBuffer.uint16()  // ignore rangeShift

        val result = mutableMapOf<Long, FontTable>()

        for (i in 0 until numTables) {
            val tag = fontBuffer.uint32()
            fontBuffer.uint32()  // ignore checkSum
            val offset = fontBuffer.uint32()
            val length = fontBuffer.uint32()

            result.put(tag, FontTable(tag, offset.toInt(), length.toInt()))
        }

        result
    }

    fun parseStyle(): FontStyle {
        val os2Table = tableMap[TAG_OS_2]
        if (os2Table == null) return FontStyle(400, false)

        val weight = fontBuffer.uint16(os2Table.offset + 4)  // usWeightClass
        val selection = fontBuffer.uint16(os2Table.offset + 62)  // fsSelection

        return FontStyle(weight, (selection and 1) != 0)
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

    private fun parseCmapFormat12(offset: Long): SparseBitSet {
        fontBuffer.position(offset)

        fontBuffer.uint16()  // ignore format since we already know it
        fontBuffer.uint16()  // ignore reserved
        fontBuffer.uint32()  // ignore length
        fontBuffer.uint32()  // ignore language
        val numGroups = fontBuffer.uint32()

        val sbsBuilder = SparseBitSet.Builder()
        for (i in 0 until numGroups) {
            val startCharCode = fontBuffer.uint32()
            val endCharCode = fontBuffer.uint32()
            fontBuffer.uint32()  // ignore startGlyphId

            sbsBuilder.append(startCharCode, endCharCode + 1)
        }
        return sbsBuilder.build()
    }

    fun parseCoverage(): SparseBitSet {
        val cmapTable = tableMap[TAG_cmap] ?: return SparseBitSet.Builder().build()
        fontBuffer.position(cmapTable.offset)
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

        // There is no supported cmap table. return empty coverage.
        if (highestScore == Int.MAX_VALUE) return SparseBitSet.Builder().build()

        fontBuffer.position(cmapTable.offset + highestTableOffset)
        var format = fontBuffer.uint16()
        if (format == 12) {
            return parseCmapFormat12(cmapTable.offset + highestTableOffset)
        } else if (format == 4){
            TODO("Format 4 is not yet supported.")
        } else {
            throw RuntimeException("Cmap format 4 or format 12 is expected.")
        }
    }
}