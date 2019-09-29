package com.nona.fontutil.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.net.Uri
import androidx.annotation.FontRes
import com.nona.fontutil.base.FileUtil
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.base.SparseBitSet
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.core.otparser.NameRecord
import com.nona.fontutil.core.otparser.OpenTypeParser
import java.io.IOException
import java.nio.ByteBuffer

/**
 * A class represents a single font file
 */
class Font private constructor(
    /**
     * A byte buffer of this font file.
     */
    val fontBuffer: ByteBuffer,
    /**
     * A Typeface object associated with this font object.
     */
    val typeface: Typeface,
    /**
     * A resolved font style of this font file.
     */
    val style: FontStyle
) {

    class Builder {
        private val fontBuffer: ByteBuffer
        private val typeface: Typeface
        private var fontStyle: FontStyle? = null

        constructor(assets: AssetManager, filePath: String) {
            fontBuffer = IOUtil.mmap(assets, filePath)
            typeface = Typeface.createFromAsset(assets, filePath)
                ?: throw IOException("Failed to create Typeface from asset:$filePath.")
        }

        @SuppressLint("ResourceType")
        constructor(context: Context, @FontRes fontResId: Int) {
            var fontBuffer: ByteBuffer? = null
            var typeface: Typeface? = null
            FileUtil.copyToTemporaryFile(context, fontResId).use {
                fontBuffer = IOUtil.mmap(it.file)
                typeface = Typeface.createFromFile(it.file)
                    ?: throw IOException("Failed to create Typeface from resource")
            }
            this.fontBuffer = fontBuffer!!
            this.typeface = typeface!!
        }

        constructor(context: Context, uri: Uri) {
            var fontBuffer: ByteBuffer? = null
            var typeface: Typeface? = null

            FileUtil.copyToTemporaryFile(context, uri).use {
                fontBuffer = IOUtil.mmap(it.file)
                typeface = Typeface.createFromFile(it.file)
                    ?: throw IOException("Failed to create Typeface from resource")
            }
            this.fontBuffer = fontBuffer!!
            this.typeface = typeface!!
        }

        fun setStyle(fontStyle: FontStyle): Builder {
            this.fontStyle = fontStyle
            return this
        }

        fun build(): Font {
            val fontStyle = fontStyle ?: OpenTypeParser(fontBuffer).parseStyle()
            return Font(fontBuffer, typeface, fontStyle)
        }
    }

    /**
     * The character coverage store in cmap table.
     */
    val cmapCoverage: SparseBitSet by lazy { OpenTypeParser(fontBuffer).parseCoverage() }

    /**
     * The family name stored in name table.
     */
    val nameRecord: NameRecord by lazy { OpenTypeParser(fontBuffer).parseName() }

    /**
     * Returns true if the character is supported by this font.
     */
    operator fun contains(codePoint: Int): Boolean = codePoint in cmapCoverage
}