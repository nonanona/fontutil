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
    private val head = HeadParser.getHead(fontBuffer, getTableOffset(TAG_head))
    private val maxp = MaxpParser.getMaxProfile(fontBuffer, getTableOffset(TAG_maxp))
    private val cmapOffset: Long

    init {
        val cmapTableOffset = getTableOffset(TAG_cmap)
        val cmapSubOffset = CMapParser.getPreferredCmapOffset(fontBuffer, cmapTableOffset)
        if (cmapSubOffset == 0L) throw RuntimeException("Supported cmap table not found")
        cmapOffset = cmapSubOffset + cmapTableOffset
    }

    fun getTableOffset(tag: Long) =
        tables.get(tag) ?: throw RuntimeException("${tag.toTagName()} not found")

    fun getGlyphId(cp: Int) = CMapParser.getGlyphId(fontBuffer, cmapOffset, cp)

    fun getGlyph(glyphId: Int):Glyph {
        val glyphOffset = LocaParser.getGlyphOffset(
            buffer = fontBuffer,
            locaOffset = getTableOffset(TAG_loca),
            locaType = head.indexToLocFormat,
            maxGlyph = maxp.numGlyph,
            glyphId = glyphId
        )

        return GlyfParser.getGlyph(fontBuffer, getTableOffset(TAG_glyf) + glyphOffset, head.unitsPerEm)
    }

    fun getGlyphPath(glyphId: Int, textSize: Float): Path {
        return getGlyph(glyphId).toPath(unitPerEm = head.unitsPerEm, textSize = textSize)
    }

}