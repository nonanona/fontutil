package com.nona.fontutil.core.otparser

import android.util.SparseArray
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint16
import com.nona.fontutil.base.uint8
import java.lang.RuntimeException
import java.nio.ByteBuffer

data class CFFMetadata(
    val cffOffset: Long,
    val offSize: Int,
    val topDict: TopDict,
    val stringList: List<String>?,
    val globalSubrOffset: Int,
    val globalBias: Int
)

data class TopDict(
    val charsetOffset: Int,
    val charStringOffset: Int,
    val fdSelectOffset: Int,
    val fdArrayOffset: Int,
    val charStringType: Int
)

data class FDMetadata(
    val defaultWidth: Int,
    val subrOffset: Int,
    val localBias: Int
)

const val NIBBLE_STR = "0123456789.EEX-\u0000"

private fun checkHeader(buffer: ByteBuffer): Int {
    // Parse Header
    checkFormat(buffer.uint8() == 0x01) { "Major version must be 1" }
    checkFormat(buffer.uint8() == 0x00) { "Minor version must be 0" }
    checkFormat(buffer.uint8() == 0x04) { "Header size must be 4"}
    return buffer.uint8()
}

private fun skipNameIndex(buffer: ByteBuffer) {
    checkFormat(buffer.uint16() == 0x01) { "OpenType/CFF only contains single font" }
    val offsetSize = buffer.uint8()
    buffer.position(buffer.position() + offsetSize)
    val lastOffset = buffer.offset(offsetSize)
    buffer.position(buffer.position() + lastOffset - 1)
}

private fun parseTopDict(buffer: ByteBuffer): TopDict {
    checkFormat(buffer.uint16() == 0x01) { "OpenType/CFF only contains single font" }
    val offsetSize = buffer.uint8()
    checkFormat(buffer.offset(offsetSize) == 0x01L) { "DICT data must start immediately" }
    val dictEnd = buffer.offset(offsetSize)
    val endPosition = buffer.position() + dictEnd - 1

    var charset = -1
    var charString = -1
    var charStringType = 2 // default is 2
    var fdSelectOffset = -1
    var fdArrayOffset = -1
    val pool = DictValuePool()
    val pending = mutableListOf<DictValue>()
    while (buffer.position() < endPosition) {
        val value = pool.get()
        readDictValue(buffer, value)
        if (value.type == ValueType.KEY) {
            when (value.intValue) {
                15 -> charset = pending[0].intValue
                17 -> charString = pending[0].intValue
                1206 -> charStringType = pending[0].intValue
                1236 -> fdArrayOffset = pending[0].intValue
                1237 -> fdSelectOffset = pending[0].intValue
            }
            pending.forEach { pool.recycle(it) }
            pending.clear()
        } else {
            pending.add(value)
        }
    }

    return TopDict(
        charsetOffset = charset,
        charStringOffset = charString,
        fdArrayOffset = fdArrayOffset,
        fdSelectOffset = fdSelectOffset,
        charStringType = charStringType
    )
}

private fun skipStringIndex(buffer: ByteBuffer): List<String> {
    val strNumber = buffer.uint16()
    val offsetSize = buffer.uint8()
    return List(strNumber + 1) {
        buffer.offset(offsetSize)
    }.zipWithNext { start, end ->
        (end - start).toInt()
    }.map {
        ByteArray(it) { buffer.get() }
    }.map {
        it.toString(Charsets.UTF_8)
    }
}

private fun readCharset(buffer: ByteBuffer, charsetOffset: Long) {
    buffer.position(charsetOffset)
    checkFormat(buffer.uint8() == 2) { "Unsupported Charset" }
    val sid = buffer.uint16()
    val nLeft = buffer.uint16()
    if (sid != 1 || nLeft != 0xFFFD) {
        TODO("Custom mapping is not yet supported")
    }
}

private fun getFDIndex(buffer: ByteBuffer, cff: CFFMetadata, glyphId: Int): Int {
    buffer.position(cff.cffOffset + cff.topDict.fdSelectOffset)
    checkFormat(buffer.uint8() == 0x03) { "FDSelect Format 3 is only supported" }
    val nRanges = buffer.uint16()

    var prevFDIndex = -1
    for (i in 0 until nRanges) { // TODO binary search
        val firstIndex = buffer.uint16()
        val fdIndex = buffer.uint8()
        if (glyphId < firstIndex) {
            return prevFDIndex
        }
        prevFDIndex = fdIndex
    }
    return prevFDIndex
}

private fun computeLocalBias(charStringType: Int, nSubr: Int): Int {
    return if (charStringType == 1) {
        0
    } else if (nSubr < 1240) {
        107
    } else if (nSubr < 33900) {
        1131
    } else {
        32768
    }
}

private fun getFDMetadata(buffer: ByteBuffer, cff: CFFMetadata, glyphId: Int): FDMetadata {
    val fdIndex = getFDIndex(buffer, cff, glyphId)  // TODO cache

    val (dictStart, dictEnd) = readIndex(buffer, cff.cffOffset + cff.topDict.fdArrayOffset, fdIndex)

    val pool = DictValuePool()
    val pending = mutableListOf<DictValue>()

    // Reading Font DICT
    var privateDictSize = -1
    var privateDictOffset = -1
    buffer.position(dictStart)
    while (buffer.position() < dictEnd) {
        val value = pool.get()
        readDictValue(buffer, value)
        if (value.type == ValueType.KEY) {
            if (value.intValue == 18) { // Private DICT
                privateDictSize = pending[0].intValue
                privateDictOffset = pending[1].intValue
            }
            pending.forEach { pool.recycle(it) }
            pending.clear()
        } else {
            pending.add(value)
        }
    }

    if (privateDictOffset == -1 || privateDictOffset == -1) {
        throw RuntimeException("Private DICT not found")
    }

    // Read Private DICT
    buffer.position(cff.cffOffset + privateDictOffset)
    val end = buffer.position() + privateDictSize
    var defaultWidth = -1
    var subrOffset = -1
    while (buffer.position() < end) {
        val value = pool.get()
        readDictValue(buffer, value)
        if (value.type == ValueType.KEY) {
            when (value.intValue) {
                20 -> defaultWidth = pending[0].intValue
                19 -> subrOffset = pending[0].intValue
            }
            pending.forEach { pool.recycle(it) }
            pending.clear()
        } else {
            pending.add(value)
        }
    }

    val absSubrOffset = subrOffset + cff.cffOffset + privateDictOffset
    buffer.position(absSubrOffset)
    val subrCount = buffer.uint16()

    return FDMetadata(
        defaultWidth = defaultWidth,
        subrOffset = absSubrOffset.toInt(),
        localBias = computeLocalBias(cff.topDict.charStringType, subrCount)
    )
}

fun computeGlobalBias(buffer: ByteBuffer, topDict: TopDict): Int {
    val count = buffer.uint16()
    return computeLocalBias(topDict.charStringType, count)
}


object CFFParser {
    fun getMetadata(buffer: ByteBuffer, cffOffset: Long): CFFMetadata {
        buffer.position(cffOffset)

        // Parse Header. Only global header is necessary
        val globalOffsetSize = checkHeader(buffer)

        // Parse Name Index
        // We don't care about name entries. skipping
        skipNameIndex(buffer)

        // Parse Top DICT Index
        val dict = parseTopDict(buffer)

        // TODO: make this optional
        val stringList = skipStringIndex(buffer)

        // After StringIndex, Global subr
        val globalSubrOffset = buffer.position()
        val globalBias = computeGlobalBias(buffer, dict)

        readCharset(buffer, cffOffset + dict.charsetOffset)

        return CFFMetadata(
            cffOffset = cffOffset,
            offSize = globalOffsetSize,
            topDict = dict,
            stringList = stringList,
            globalSubrOffset = globalSubrOffset,
            globalBias = globalBias
        )
    }

    fun getGlyph(buffer: ByteBuffer, glyphId: Int, cff: CFFMetadata, unitPerEm: Int): Glyph {
        val fd = getFDMetadata(buffer, cff, glyphId)
        val list = execCharString(buffer, cff, fd, glyphId)
        return OutlineGlyph(
            type = OutlineType.CUBIC_BEZIER_CURVE,
            contours = list,
            unitPerEm = unitPerEm
        )
    }
}