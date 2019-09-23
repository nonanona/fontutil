package com.nona.fontutil.systemfont

import android.util.Xml
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontVariation
import com.nona.fontutil.core.otparser.FontStyle
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.regex.Pattern

private const val SYSTEM_FONTS_XML = "/system/etc/fonts.xml"
private const val SYSTEM_FONT_DIR = "/system/fonts"

enum class FontVariant { UNSPECIFIED, COMPACT, ELEGANT }

data class SystemFont internal constructor(
    val file: File,
    val index: Int,
    val style: FontStyle,
    val variant: FontVariant,
    val langauge: String?,
    val axes: Array<FontVariation.Axis>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SystemFont

        if (file != other.file) return false
        if (index != other.index) return false
        if (style != other.style) return false
        if (variant != other.variant) return false
        if (langauge != other.langauge) return false
        if (!axes.contentEquals(other.axes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + index
        result = 31 * result + style.hashCode()
        result = 31 * result + variant.hashCode()
        result = 31 * result + (langauge?.hashCode() ?: 0)
        result = 31 * result + axes.contentHashCode()
        return result
    }
}

class SystemFonts private constructor(){

    companion object {

        fun retrieve(): Set<SystemFont> {
            return FileInputStream(SYSTEM_FONTS_XML).use {
                val parser = Xml.newPullParser().apply {
                    setInput(it, null)
                    nextTag()
                }

                val fonts = mutableSetOf<SystemFont>()
                parser.require(XmlPullParser.START_TAG, null, "familyset")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) continue
                    if (parser.name == "family") {
                        readFamily(parser, fonts)
                    } else {
                        skip(parser)
                    }
                }

                fonts
            }
        }

        private fun skip(parser: XmlPullParser) {
            var depth = 1
            while (depth > 0) {
                when (parser.next()) {
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_TAG -> depth--
                }
            }
        }
        private val FILENAME_WHITESPACE_PATTERN = Pattern.compile("^[ \\n\\r\\t]+|[ \\n\\r\\t]+$")

        private fun readFont(
            parser: XmlPullParser,
            language: String?,
            variant: FontVariant
        ): SystemFont {
            val index = parser.getAttributeValue(null, "index")?.let { Integer.parseInt(it)} ?: 0
            val weight = parser.getAttributeValue(null, "weight")?.let { Integer.parseInt(it)} ?: 0
            val isItalic = "italic" == parser.getAttributeValue(null, "style")
            val filename = StringBuilder()
            val axes = ArrayList<FontVariation.Axis>()
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType == XmlPullParser.TEXT) {
                    filename.append(parser.text)
                }
                if (parser.eventType != XmlPullParser.START_TAG) continue
                val tag = parser.name
                if (tag == "axis") {
                    axes.add(readAxis(parser))
                } else {
                    skip(parser)
                }
            }
            val sanitizedName = FILENAME_WHITESPACE_PATTERN.matcher(filename).replaceAll("")
            return SystemFont(
                File("$SYSTEM_FONT_DIR/$sanitizedName"),
                index,
                FontStyle(weight, isItalic),
                variant,
                language,
                axes.toTypedArray()
            )
        }

        private fun readAxis(parser: XmlPullParser): FontVariation.Axis {
            val tagStr = parser.getAttributeValue(null, "tag")
            val styleValueStr = parser.getAttributeValue(null, "stylevalue")
            skip(parser)
            if (tagStr == null || tagStr.length != 4) throw IOException("Invalid Tag found")
            if (styleValueStr == null) throw IOException("Invalid axis value found")
            val intTag = (tagStr[0].toInt() shl 24) or (tagStr[1].toInt() shl 16) or
                    (tagStr[2].toInt() shl 8) or (tagStr[3].toInt())
            return FontVariation.Axis(intTag, styleValueStr.toFloat())
        }

        private fun readFamily(parser: XmlPullParser, out: MutableSet<SystemFont>) {
            val lang = parser.getAttributeValue("", "lang")
            val variant = when(parser.getAttributeValue(null, "variant")) {
                "compact" -> FontVariant.COMPACT
                "elegant" -> FontVariant.ELEGANT
                else -> FontVariant.UNSPECIFIED
            }
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                val tag = parser.name
                if (tag == "font") {
                    out.add(readFont(parser, lang, variant))
                } else {
                    skip(parser)
                }
            }
        }

    }
}