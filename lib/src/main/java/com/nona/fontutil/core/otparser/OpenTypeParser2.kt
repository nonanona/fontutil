package com.nona.fontutil.core.otparser

import androidx.collection.LongSparseArray
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import com.nona.fontutil.base.uint32
import java.io.IOException
import java.nio.ByteBuffer

object OpenTypeParser2 {

    fun getOffsetTableOffset(
        buffer: ByteBuffer,
        index: Int = 0,
        offset: Int = 0
    ): Long {
        buffer.position(offset)
        val sfntVersion = buffer.uint32()
        if (sfntVersion == TAG_ttcf) {
            // The given file is TTC, read specified index.
            buffer.uint16()  // ignore majorVersion
            buffer.uint16()  // ignore minorVersion
            val numFonts = buffer.uint32()

            for (i in 0 until numFonts) {
                val bufferOffset = buffer.uint32()
                if (i.toInt() == index) {
                    return bufferOffset
                }
            }
            return -1
        }

        return 0
    }

    fun getTableOffsets(buffer: ByteBuffer, index: Int, offset: Int = 0): LongSparseArray<Long> {
        val tableHead = getOffsetTableOffset(buffer, index, offset)
        buffer.position(tableHead)

        val sfntVersion = buffer.uint32()
        if (sfntVersion != SFNT_TAG_OTTO && sfntVersion != SFNT_VERSION_1_0) {
            throw IOException("sfntVersion is invalid ${sfntVersion}")
        }

        val numTables = buffer.uint16()
        buffer.uint16()  // ignore searchRange
        buffer.uint16()  // ignore entrySelector
        buffer.uint16()  // ignore rangeShift

        val result = LongSparseArray<Long>()
        for (i in 0 until numTables) {
            val tag = buffer.uint32()
            buffer.uint32()  // ignore checkSum
            val tableOffset = buffer.uint32()
            buffer.uint32()  // ignore length

            result.put(tag, tableOffset)
        }
        return result
    }





}