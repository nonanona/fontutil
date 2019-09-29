package com.nona.fontutil.core

/**
 * A collection of the font files.
 */
class FontCollection(val families: Array<FontFamily>) {
    init {
        if (families.isEmpty()) {
            throw RuntimeException("Families must not be empty.")
        }
    }
}