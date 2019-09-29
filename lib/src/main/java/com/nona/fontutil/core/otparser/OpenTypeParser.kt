package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.SparseBitSet
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

data class FontStyle(val weight: Int, val italic: Boolean)
data class NameRecord(val familyName: String, val subFamilyName: String)

// The same logic in Minikin
fun styleDistance(a: FontStyle, b: FontStyle): Int {
    return ((a.weight - b.weight) / 100) + (if (a.italic == b.italic) 2 else 0)
}

private fun Int.toUnsigned(): Long = toLong() and 0xFFFF_FFFFL
private fun Short.toUnsigned(): Int = toInt() and 0xFFFF
private fun Byte.toUnsigned(): Int = toInt() and 0xFF

private fun ByteBuffer.uint32(): Long = int.toUnsigned()
private fun ByteBuffer.int32(): Int = int
private fun ByteBuffer.uint16(): Int = short.toUnsigned()
private fun ByteBuffer.int16(): Int = short.toInt()
private fun ByteBuffer.uint8(): Int = get().toUnsigned()
private fun ByteBuffer.int8(): Int = get().toInt()

private fun ByteBuffer.uint32(i: Int): Long = getInt(i).toUnsigned()
private fun ByteBuffer.int32(i: Int): Int = getInt(i)
private fun ByteBuffer.uint16(i: Int): Int = getShort(i).toUnsigned()
private fun ByteBuffer.int16(i: Int): Int = getShort(i).toInt()
private fun ByteBuffer.uint8(i: Int): Int = get(i).toUnsigned()
private fun ByteBuffer.int8(i: Int): Int = get(i).toInt()

private fun ShortBuffer.uint16(): Int = get().toUnsigned()
private fun ShortBuffer.intt16(): Int = get().toInt()

// We are not supporting over 2GB font files. Just cast to Int.
private fun ByteBuffer.position(i: Long) = position(i.toInt())

private const val SFNT_VERSION_1_0 = 0x0001_0000L
private const val SFNT_TAG_OTTO = 0x4F_54_54_4FL

private const val TAG_ttcf = 0x74_74_63_66L
private const val TAG_OS_2 = 0x4F_53_2F_32L
private const val TAG_cmap = 0x63_6D_61_70L
private const val TAG_name = 0x6E_61_6D_65L

class OpenTypeParser(fontBuffer: ByteBuffer, val index: Int = 0) {

    private val fontBuffer = fontBuffer.slice().apply { order(ByteOrder.BIG_ENDIAN) }

    private fun getTableOffset(tableTag: Long, bufferOffset:Int = 0): Int {
        fontBuffer.position(bufferOffset)
        val sfntVersion = fontBuffer.uint32()

        if (sfntVersion == TAG_ttcf) {
            // The given file is TTC, read specified index.
            fontBuffer.uint16()  // ignore majorVersion
            fontBuffer.uint16()  // ignore minorVersion
            val numFonts = fontBuffer.uint32()

            for (i in 0 until numFonts) {
                val fontBufferOffset = fontBuffer.uint32()
                if (i.toInt() == index) {
                    return getTableOffset(tableTag, fontBufferOffset.toInt())
                }
            }
            return -1
        }

        if (sfntVersion != SFNT_TAG_OTTO && sfntVersion != SFNT_VERSION_1_0) {
            throw IOException("sfntVersion is invalid ${sfntVersion}")
        }
        val numTables = fontBuffer.uint16()
        fontBuffer.uint16()  // ignore searchRange
        fontBuffer.uint16()  // ignore entrySelector
        fontBuffer.uint16()  // ignore rangeShift

        for (i in 0 until numTables) {
            val tag = fontBuffer.uint32()
            fontBuffer.uint32()  // ignore checkSum
            val offset = fontBuffer.uint32()
            fontBuffer.uint32()  // ignore length

            if (tableTag == tag) {
                return offset.toInt()
            }
        }
        return -1
    }

    fun parseStyle(): FontStyle {
        val offset = getTableOffset(TAG_OS_2)
        if (offset == -1) return FontStyle(400, false)

        val weight = fontBuffer.uint16(offset + 4)  // usWeightClass
        val selection = fontBuffer.uint16(offset + 62)  // fsSelection

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

    private fun parseCmapFormat4(offset: Long): SparseBitSet {
        fontBuffer.position(offset)

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

        val sbsBuilder = SparseBitSet.Builder()
        for (i in 0 until segCount - 1) {
            val start = startCodes[i].toUnsigned()
            val end = endCodes[i].toUnsigned()
            val idRangeOffset = idRangeOffsets[i].toUnsigned()

            if (idRangeOffset == 0) {
                sbsBuilder.append(start, end + 1)  // end is inclusive
            } else {
                // TODO: Check Glyph ID which should not be zero.
                sbsBuilder.append(start, end + 1)
            }
        }

        return sbsBuilder.build()
    }

    fun parseCoverage(): SparseBitSet {
        val cmapOffset = getTableOffset(TAG_cmap)
        if (cmapOffset == -1) return SparseBitSet.Builder().build()
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

        // There is no supported cmap table. return empty coverage.
        if (highestScore == Int.MAX_VALUE) return SparseBitSet.Builder().build()

        fontBuffer.position(cmapOffset + highestTableOffset)
        val format = fontBuffer.uint16()
        if (format == 12) {
            return parseCmapFormat12(cmapOffset + highestTableOffset)
        } else if (format == 4) {
            return parseCmapFormat4(cmapOffset + highestTableOffset)
        } else {
            throw RuntimeException("Cmap format 4 or format 12 is expected.")
        }
    }

    private fun isSupportEncoding(platformId: Int, encodingId: Int, languageId: Int): Boolean {
        if (platformId == 3) {  // Windows
            if (encodingId == 1) {  // Unicode BMP
                if (languageId == 0x409) { // en-US
                    return true
                }
            }
        }
        return false
    }

    fun parseName(): NameRecord {
        val nameOffset = getTableOffset(TAG_name)
        if (nameOffset == -1) return NameRecord("", "")
        fontBuffer.position(nameOffset)

        fontBuffer.uint16()  // ignore format
        val count = fontBuffer.uint16()
        val stringOffset = fontBuffer.uint16()

        var familyNameOffset = -1
        var familyNameLength = -1
        var subFamilyNameOffset = -1
        var subFamilyNameLength = -1

        for (i in 0 until count) {  // Reading NameRecord
            val platformId = fontBuffer.uint16()
            val encodingId = fontBuffer.uint16()
            val languageId = fontBuffer.uint16()
            val nameId = fontBuffer.uint16()
            val length = fontBuffer.uint16()
            val offset = fontBuffer.uint16()

            if (!isSupportEncoding(platformId, encodingId, languageId)) continue

            // We prefer Typographic Family name (nameID = 16) over legacy Family Name (nameID = 1)
            when (nameId) {
                1 -> if (familyNameOffset == -1) {
                    familyNameOffset = offset
                    familyNameLength = length
                }
                2 -> if (subFamilyNameOffset == -1) {
                    subFamilyNameOffset = offset
                    subFamilyNameLength = length
                }
                16 -> {
                    familyNameOffset = offset
                    familyNameLength = length
                }
                17 -> {
                    subFamilyNameOffset = offset
                    subFamilyNameLength = length
                }
            }
        }

        // The String is encoded in UTF-16BE.
        val familyNameChars = CharArray(familyNameLength / 2)
        for (j in familyNameChars.indices) {
            familyNameChars[j] = fontBuffer.getChar(
                nameOffset + stringOffset + familyNameOffset + j * 2
            )
        }

        val subFamilyChars = CharArray(subFamilyNameLength / 2)
        for (j in subFamilyChars.indices) {
            subFamilyChars[j] = fontBuffer.getChar(
                nameOffset + stringOffset + subFamilyNameOffset + j * 2
            )
        }

        return NameRecord(String(familyNameChars), String(subFamilyChars))
    }
}