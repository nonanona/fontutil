package com.nona.fontutil.provider

import android.content.Context
import com.nona.fontutil.assets.CustomTagParser
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontFamily
import java.lang.IllegalArgumentException

open class FontProviderTagParser(val appContext: Context, val authority: String) : CustomTagParser {

    val fontFetcher = FontFetcher(appContext, authority)

    override fun parseFamily(attributes: Map<String, String>): FontFamily? {
        val name = attributes.get("name") ?: throw IllegalArgumentException("name attr is required")

        val list = mutableListOf<Font>()
        for (weight in 100 until 1000 step 100) {
            for (italic in arrayOf(false, true)) {
                fontFetcher.fetchSingleFont(name, weight, italic)?.let { list.add(it) }
            }
        }
        return FontFamily.Builder(list.toTypedArray()).build()
    }

    override fun parseFont(attributes: Map<String, String>): Font? {
        val name = attributes.get("name") ?: throw IllegalArgumentException("name attr is required")
        val weight = attributes.get("weight")
        val italic = attributes.get("italic")
        return fontFetcher.fetchSingleFont(name, weight?.toIntOrNull(), italic?.toBoolean())
    }

}