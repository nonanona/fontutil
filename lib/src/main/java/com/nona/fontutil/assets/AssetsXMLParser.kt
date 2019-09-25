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

class AssetsXMLParser private constructor() {
    companion object {
        fun parseFontCollectionXml(context: Context, xmlPath: String): FontCollection {
           return context.assets.open(xmlPath).use {
                val parser = Xml.newPullParser()
                parser.setInput(it, null)
                parseFontCollection(context, parser)
            }
        }

        private fun parseFontCollection(context: Context, parser: XmlPullParser): FontCollection {
            val list = mutableListOf<FontFamily>()

            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, null, "FontCollection")
            val fallbackName = parser.getAttributeValue(null, "fallback")
            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.eventType != XmlPullParser.START_TAG) continue

                list.add(
                    when (parser.name) {
                        "FontFamily" -> parseFontFamily(context, parser)
                        "AssetDirectoryFontFamily" -> parseAssetDirectoryFontFamily(context, parser)
                        else -> throw RuntimeException("Unknown Tag: ${parser.name}")
                    }
                )
            }

            val fallback = Typeface.create(fallbackName, Typeface.NORMAL) ?: Typeface.DEFAULT

            return FontCollection(list.toTypedArray(), fallback)
        }

        private fun parseFontFamily(context: Context,parser: XmlPullParser): FontFamily {
            val list = mutableListOf<Font>()

            //parser.nextTag()
            val familyName = parser.getAttributeValue(null, "name")
            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.eventType != XmlPullParser.START_TAG) continue

                list.add(
                    when (parser.name) {
                        "AssetFont" -> parseAssetFont(context, parser)
                        else -> throw RuntimeException("Unknown Tag: ${parser.name}")
                    }
                )
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