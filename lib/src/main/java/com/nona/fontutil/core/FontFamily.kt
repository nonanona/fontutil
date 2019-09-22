package com.nona.fontutil.core

import java.lang.IllegalArgumentException

data class FontFamily private constructor(
    val fonts: Array<Font>,
    val name: String
) {

    class Builder(val fonts: Array<Font>) {
        var name: String? = null

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun build(): FontFamily {
            return FontFamily(
                fonts,
                name ?: ""
            )
        }
    }

    init {
        if (fonts.isEmpty()) throw IllegalArgumentException("Font must contains at least one font")
    }

    // cmap coverage is likely to be used in UI thread, so compute it when it is created.
    val cmapCoverage = fonts[0].cmapCoverage

    operator fun contains(codePoint: Int): Boolean = codePoint in cmapCoverage
}