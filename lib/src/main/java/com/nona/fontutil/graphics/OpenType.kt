package com.nona.fontutil.graphics

import android.content.ContentResolver
import android.content.res.AssetManager
import android.graphics.Path
import android.net.Uri
import android.util.SparseArray
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.core.otparser.*
import java.io.File
import java.lang.RuntimeException
import java.nio.ByteBuffer



class OpenType(buffer: ByteBuffer, val index: Int) {
    constructor(file: File, index: Int = 0, offset: Long = 0, length: Long? = null)
            : this(IOUtil.mmap(file = file, offset = offset, length = length), index)
    constructor(assets: AssetManager, file: String, index: Int = 0): this(IOUtil.mmap(assets, file), index)
    constructor(resolver: ContentResolver, uri: Uri, index: Int = 0): this(IOUtil.mmap(resolver, uri), index)

    private val fontBuffer = buffer.slice().apply {
        order(java.nio.ByteOrder.BIG_ENDIAN)
    }

    private val tables = OpenTypeParser2.getTableOffsets(fontBuffer, index)
    val head = HeadParser.getHead(fontBuffer, getTableOffset(TAG_head))
    val maxp = MaxpParser.getMaxProfile(fontBuffer, getTableOffset(TAG_maxp))
    val hhea = HheaParser.getMetrix(fontBuffer, getTableOffset(TAG_hhea))
    private val cmapOffset: Long
    private val cff: CFFMetadata?

    init {
        val cmapTableOffset = getTableOffset(TAG_cmap)
        val cmapSubOffset = CMapParser.getPreferredCmapOffset(fontBuffer, cmapTableOffset)
        if (cmapSubOffset == 0L) throw RuntimeException("Supported cmap table not found")
        cmapOffset = cmapSubOffset + cmapTableOffset

        cff = tables.get(TAG_CFF)?.let { cffOffset ->
            CFFParser.getMetadata(fontBuffer, cffOffset)
        }
    }

    fun getTableOffset(tag: Long) =
        tables.get(tag) ?: throw RuntimeException("${tag.toTagName()} not found")

    fun getGlyphId(cp: Int) = CMapParser.getGlyphId(fontBuffer, cmapOffset, cp)

    fun getGlyph(glyphId: Int):Glyph {
        val (advance, lsb) = HmtxParser.getMetrix(
            fontBuffer,
            getTableOffset(TAG_hmtx),
            hhea.numberOfHMetrics,
            glyphId
        )
        if (cff == null) { // TrueType font. Use loca/glyf table
            val glyphOffset = LocaParser.getGlyphOffset(
                buffer = fontBuffer,
                locaOffset = getTableOffset(TAG_loca),
                locaType = head.indexToLocFormat,
                maxGlyph = maxp.numGlyph,
                glyphId = glyphId
            )

            return OutlineGlyph(
                type = OutlineType.QUADRATIC_BEZIER_CURVE,
                contours = GlyfParser.getOutline(
                    fontBuffer,
                    getTableOffset(TAG_glyf) + glyphOffset
                ),
                unitPerEm = head.unitsPerEm,
                advance = advance,
                lsb = lsb
            )
        } else { // CFF font
            return OutlineGlyph(
                type = OutlineType.CUBIC_BEZIER_CURVE,
                contours = CFFParser.getOutline(fontBuffer, glyphId, cff),
                unitPerEm = head.unitsPerEm,
                advance = advance,
                lsb = lsb
            )
        }
    }

    fun shapeText(str: String): List<Glyph> {
        var i = 0
        val glyphIds = mutableListOf<Int>()
        while (i < str.length) {
            val cp = str.codePointAt(i)
            i += Character.charCount(cp)
            glyphIds.add(getGlyphId(cp))
        }
        // TODO: GSUB process
        return glyphIds.map {
            getGlyph(it)
        }
    }
}