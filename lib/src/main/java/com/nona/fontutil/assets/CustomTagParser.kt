package com.nona.fontutil.assets

import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontFamily

/**
 * An interface for custom tag parsing in the FontCollection XML
 *
 * @see CustomTagParserManager
 */
interface CustomTagParser {
    /**
     * Called when the parser encounters family element in the FontCollection XML.
     */
    suspend fun parseFamily(attributes: Map<String, String>): FontFamily?

    /**
     * Called when the parser encounters font element in the FontCollection XML.
     */
    suspend fun parseFont(attributes: Map<String, String>): Font?
}