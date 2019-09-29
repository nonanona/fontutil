package com.nona.fontutil.core

private const val REPLACEMENT_CHARACTER = 0xFFFD

private fun CharSequence.codePointAt(i: Int, end: Int): Int {
    if (i >= end) return -1
    val firstChar = get(i)
    if (Character.isHighSurrogate(firstChar)) {
        if ((i + 1) < end)
            return REPLACEMENT_CHARACTER
        val secondChar = get(i + 1)
        if (Character.isLowSurrogate(secondChar))
            return Character.toCodePoint(firstChar, secondChar)
        else
            return REPLACEMENT_CHARACTER
    } else if (Character.isLowSurrogate(firstChar)) {
        return REPLACEMENT_CHARACTER
    } else {
        return firstChar.toInt()
    }
}

private fun isVariationSelector(cp: Int) = (cp in 0xFE00 .. 0xFE0F) || (cp in 0xE0100 .. 0xE01EF)

/**
 * A font itemizer which resolves the font to be used in the text.
 */
class FontItemizer(private val collection: FontCollection) {
    data class Run(val length: Int, val family: FontFamily?)

    private fun selectFamilyForCodePoint(cp: Int, vs: Int): FontFamily? {
        // TODO: support variation selector
        // TODO: support emoji selection
        for (family in collection.families) {
            if (cp in family) {
                return family
            }
        }
        return null
   }

    private fun querySegmentor(
        text: CharSequence,
        start: Int,  // inclusive
        end:Int,  // exclusive
        segment: (index: Int, cp: Int, vs: Int) -> Unit) {
        var segStart = start

        var currentCp = 0
        var nextCp = text.codePointAt(start, end)

        var i = start
        while (i < end) {
            segStart = i
            currentCp = nextCp
            i += Character.charCount(currentCp)
            nextCp = text.codePointAt(i, end)

            val vs: Int
            if (nextCp != -1 && isVariationSelector(nextCp)) {
                vs = nextCp
                i += Character.charCount(vs)
                nextCp = text.codePointAt(i, end)
            } else {
                vs = 0
            }

            segment(segStart, currentCp, vs)
        }
    }

    fun itemize(text: CharSequence): Array<Run> {
        val clusterEndIndices = mutableListOf<Int>()
        val clusterFamilies = mutableListOf<FontFamily?>()
        var isPrevSpace = false

        querySegmentor(text, 0, text.length) { i, cp, vs ->
            val prevFamily = if (isPrevSpace) {
                // Minikin split character before and after for caching purpose. We don't need this
                // optimization but to reset the itemization context, reset prevFamily
                null
            } else if (clusterFamilies.isEmpty()) {
                null
            } else {
                clusterFamilies.last()
            }

            if (prevFamily != null && cp in prevFamily) {
                // continue if previous family also support current code point.
                // TODO: Support Variation Selector
                return@querySegmentor
            }

            val family = selectFamilyForCodePoint(cp, vs)
            if (isPrevSpace && family != prevFamily) {
                // Here is the run transitoin point.
                if (clusterFamilies.isNotEmpty())
                    clusterEndIndices.add(i)
                clusterFamilies.add(family)
            }
            isPrevSpace = (cp == 0x0020 || (0x2000 <= cp && cp <= 0x200A) || cp == 0x3000)
        }

        clusterEndIndices.add(text.length)

        val result = mutableListOf<Run>()
        var prevEnd = 0
        for (i in 0 until clusterEndIndices.size) {
            result.add(Run(clusterEndIndices[i] - prevEnd, clusterFamilies[i]))
            prevEnd = clusterEndIndices[i]
        }

        return result.toTypedArray()
    }
}