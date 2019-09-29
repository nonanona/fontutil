package com.nona.fontutil.assets

/**
 * Manages the custom FontCollection tag parser.
 *
 * The FontCollection XML has single root element, FontCollection.
 * The FontCollection element can hold multiple FontFamily elements. FontFamily and
 * AssetDirectoryFontFamily are reserved as a default tag parser. You can add your own FamilyParser
 * by registering custom tag parser to this object.
 *
 * The FontFamily can hold multiple Font elements. AssetFont is reserved as a default tag parser.
 * You can add your own FamilyParser by registering custom tag parser to this object.
 *
 * For example,
 *
 * <pre>
 * <FontCollection>
 *     <FontFamily>
 *         <YourFont path="aaa/bbb/ccc" />
 *     <FontFamily>
 *
 *     <YourFontFamily name="myawesomefont" />
 * </FontCollection>
 * </pre>
 *
 * In this example, by registering custom tag parser with "Your" key, you can provide your custom
 * tag parser both for YourFont and YourFontFamily.
 */
object CustomTagParserManager {
    private val lock = Object()

    private val tagMap = mutableMapOf<String, CustomTagParser>()

    /**
     * Registers a custom tag parser.
     * By calling with "XXX" as a tag argument, the custom tag parser will be called back if parser
     * see <XXXFont> and <XXXFontFamily> tag in your XML.
     */
    fun register(tag: String, parser: CustomTagParser) {
        synchronized(lock) {
            if (tag in tagMap) {
                throw RuntimeException("$tag is already registered")
            }
            tagMap.put(tag, parser)
        }
    }

    internal fun obtainParser(tag: String): CustomTagParser? {
        synchronized(lock) {
            return tagMap[tag]
        }
    }
}
