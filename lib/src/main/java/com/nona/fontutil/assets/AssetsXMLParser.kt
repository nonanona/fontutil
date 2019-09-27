package com.nona.fontutil.assets

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.util.Xml
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.core.FontFamily
import com.nona.fontutil.core.otparser.FontStyle
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.lang.RuntimeException

private fun XmlPullParser.getAttributes(): Map<String, String> {
    val res = mutableMapOf<String, String>()
    for (i in 0 until attributeCount) {
        val name = getAttributeName(i)
        val value = getAttributeValue(null, name)

        res.put(name, value)
    }
    return res
}

class AssetsXMLParser private constructor() {
    companion object {
        fun parseFontCollectionXml(context: Context, xmlPath: String): FontCollection {
           return context.assets.open(xmlPath).use {
                val parser = Xml.newPullParser()
                parser.setInput(it, null)
                parseFontCollection(context, parser)
            }
        }

        private fun getCustomFamilyParser(tag: String): CustomTagParser? {
            if (!tag.endsWith("FontFamily")) {
                return null
            }
            val key = tag.substring(0, tag.length - 10)
            return CustomTagParserManager.obtainParser(key)
        }

        private fun parseFontCollection(context: Context, parser: XmlPullParser): FontCollection {
            val list = mutableListOf<FontFamily>()

            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, null, "FontCollection")
            val fallbackName = parser.getAttributeValue(null, "fallback")
            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.eventType != XmlPullParser.START_TAG) continue

                val family = when (parser.name) {
                    "FontFamily" -> parseFontFamily(context, parser)
                    "AssetDirectoryFontFamily" -> parseAssetDirectoryFontFamily(context, parser)
                    else -> {
                        val customParser = getCustomFamilyParser(parser.name)
                            ?: throw RuntimeException("Unknown Tag: ${parser.name}")
                        customParser.parseFamily(parser.getAttributes())
                    }
                } ?: continue
                list.add(family)
            }

            val fallback = Typeface.create(fallbackName, Typeface.NORMAL) ?: Typeface.DEFAULT

            return FontCollection(list.toTypedArray(), fallback)
        }

        private fun getCustomFontParser(tag: String): CustomTagParser? {
            if (!tag.endsWith("Font")) {
                return null
            }
            val key = tag.substring(0, tag.length - 4)
            return CustomTagParserManager.obtainParser(key)
        }

        private fun parseFontFamily(context: Context,parser: XmlPullParser): FontFamily {
            val list = mutableListOf<Font>()

            val familyName = parser.getAttributeValue(null, "name")
            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.eventType != XmlPullParser.START_TAG) continue

                val font = when (parser.name) {
                    "AssetFont" -> parseAssetFont(context, parser)
                    else -> {
                        val customParser = getCustomFontParser(parser.name)
                            ?: throw RuntimeException("Unknown Tag: ${parser.name}")
                        customParser.parseFont(parser.getAttributes())
                    }
                } ?: continue
                list.add(font)
            }

            val builder = FontFamily.Builder(list.toTypedArray())
            if (familyName != null)
                builder.name = familyName
            return builder.build()
        }

        private fun parseAssetDirectoryFontFamily(
            context: Context,parser:
            XmlPullParser
        ): FontFamily {
            val list = mutableListOf<Font>()

            //parser.nextTag()
            val path = parser.getAttributeValue(null, "path")
                ?: throw IOException("AssetDirectoryFamily must have path attribute ")
            val familyName = parser.getAttributeValue(null, "name")

            context.assets.list(path)?.forEach {
                list.add(Font.Builder(context.assets, path + "/" + it).build())
            } ?: throw IOException("$path not found")

            val builder = FontFamily.Builder(list.toTypedArray())
            if (familyName != null)
                builder.name = familyName
            return builder.build()
        }

        private fun parseAssetFont(context: Context, parser: XmlPullParser): Font {
            val path = parser.getAttributeValue(null, "path")
                ?: throw IOException("AssetFont must have path attribute")

            val builder = Font.Builder(context.assets, path)
            parser.getAttributeValue(null, "weight")?.let { weightStr ->
                parser.getAttributeValue(null, "style")?.let { styleStr ->
                    builder.setStyle(FontStyle(
                        weight = Integer.parseInt(weightStr),
                        italic = styleStr == "italic"
                    ))
                }
            }

            // TODO: Parse index/varSettings
            return builder.build()
        }
    }
}