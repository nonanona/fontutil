package com.nona.fontutil.core

import android.graphics.Typeface
import java.lang.RuntimeException


class FontCollection(
    val families: Array<FontFamily>,
    val fallback: Typeface
) {
    init {
        if (families.isEmpty()) {
            throw RuntimeException("Families must not be empty.")
        }
    }
}