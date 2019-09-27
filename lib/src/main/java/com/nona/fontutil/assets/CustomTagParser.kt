package com.nona.fontutil.assets

import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontFamily

interface CustomTagParser {
    fun parseFamily(attributes: Map<String, String>): FontFamily?
    fun parseFont(attributes: Map<String, String>): Font?
}