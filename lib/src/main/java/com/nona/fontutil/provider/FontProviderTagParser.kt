package com.nona.fontutil.provider

import android.content.Context
import com.nona.fontutil.assets.CustomTagParser
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontFamily

open class FontProviderTagParser(appContext: Context, authority: String) : CustomTagParser {

    val fontFetcher = FontFetcher(appContext, authority)

    override suspend fun parseFamily(attributes: Map<String, String>): FontFamily? {
        val name = attributes.get("name") ?: throw IllegalArgumentException("name attr is required")

        val list = mutableListOf<Font>()
        for (weight in 100 until 1000 step 100) {
            for (italic in arrayOf(false, true)) {
                fontFetcher.fetchSingleFont(name, weight, italic, true)?.let { list.add(it) }
            }
        }
        if (list.isEmpty()) return null
        return FontFamily.Builder(list.toTypedArray()).build()
    }

    override suspend fun parseFont(attributes: Map<String, String>): Font? {
        val name = attributes.get("name") ?: throw IllegalArgumentException("name attr is required")
        val weight = attributes.get("weight")
        val italic = attributes.get("italic")
        return fontFetcher.fetchSingleFont(name, weight?.toIntOrNull(), italic?.toBoolean(), true)
    }

}