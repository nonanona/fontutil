package com.nona.fontutil.base

import java.io.IOException

private const val MAX_CAPACITY = 0xFF_FFFF
private const val FULLBIT_INT = -1
private const val TOP_BIT_INT = -2147483648

/**                                                                (1 element = 256 bits = 8 dwords)
 * 0000-00FF: page = 0x00, indices[0x00] = slot[0x00] = bitmaps[0x00] = 00100010...000000010
 * 0100-01FF: page = 0x01, indices[0x01] = slot[0x01] = bitmaps[0x08] = 00001001...000001000
 * 0200-02FF: page = 0x02, indices[0x02] = slot[0x02] = bitmaps[0x10] = 00000000...000000000
 * 0300-03FF: page = 0x03, indices[0x03] = slot[0x02] = bitmaps[0x10] = 00000000...000000000
 * 0400-04FF: page = 0x04, indices[0x04] = slot[0x02] = bitmaps[0x10] = 00000000...000000000
 * 0500-05FF: page = 0x05, indices[0x05] = slot[0x02] = bitmaps[0x10] = 00000000...000000000
 * 0600-06FF: page = 0x06, indices[0x06] = slot[0x03] = bitmaps[0x18] = 00011010...000010101
 */
class SparseBitSet private constructor(
    /**
     * Index to the slot
     */
    private val indices: IntArray,

    private val bitmaps: IntArray,
    val maxValue: Int) {

    class Builder {
        private val starts = mutableListOf<Int>()
        private val ends = mutableListOf<Int>()

        fun append(start: Int, end: Int) : Builder {
            if (start == end) return this  // empty range. do nothing
            if (ends.isEmpty() || ends.last() < start) {
                starts.add(start)
                ends.add(end)
            } else if (ends.last() == start) {
                ends[ends.lastIndex] = end
            } else {
                throw RuntimeException("Unordered cmap range.")
            }
            return this
        }

        fun append(index: Int) : Builder = append(index, index + 1)

        /**
         * Returns the necessary indices size.
         *
         * For example, if maxValue is 0x1234, need to store until 0x12FF, so the capacity should
         * be 0x13.
         */
        private fun numOfIndices(): Int = page((ends.last() + 0xFF))

        private fun numOfSlots(): Int {
            var hasZerosSlot = false
            var nonZeroPageEnd = 0  // exclusive
            var numSlots = 0

            for (i in 0 until starts.size) {
                val start = starts[i]
                val end = ends[i]

                val startPage = start shr 8
                val endPage = (end - 1) shr 8

                if (startPage >= nonZeroPageEnd) {
                    if (startPage > nonZeroPageEnd) {
                        // There is a zeros page from last nonZeroPage to current start page.
                        if (!hasZerosSlot) {
                            // First to see the zeros page. assing zeros to new slot.
                            hasZerosSlot = true
                            numSlots ++
                        }
                    }
                    numSlots++  // For storing start page.
                }
                // Do not add +1 since startPage is already assigned above
                numSlots += endPage - startPage

                nonZeroPageEnd = endPage + 1
            }

            return numSlots
        }

        private inline fun page(value: Int) = value ushr 8

        /**
         * Each elemenet has 256 bits, which corresponds 8 ints in the bitmap. So n-th page is
         * n * 0x08 -th index of bitmap.
         */
        private inline fun slotToBitmapStart(slotIndex: Int) = slotIndex * 0x08

        /**
         * For example, 0x1234 is
         *   pageIndex: 0x12
         *   bit offset in the assigned slot: 0x34
         *   bitmap index in the assigned slot: 0x34 / 32 (bits per bitmap) = 0x34 >> 5 = 1
         *   bit offset in the bitmap: 0x34 & 0b11111 (0x1f) = 0x14
         *
         * For example, 0xABCD is
         *   pageIndex: 0xAB
         *   bit offset in the assigned slot: 0xCD
         *   bitmap index in the assigned slot: 0xCD / 32 (bits per bitmap) = 0xCD >> 5 = 6
         *   bit offset in the bitmap: 0xCD & 0b11111 (0x1f) = 0x0D
         */
        private inline fun bitmapIndexInSlot(start: Int) = (start and 0xFF) ushr 5
        private inline fun bitOffsetInBitmap(value: Int) = value and 0x1f

        /**
         * Returning number of bitmaps between the given range
         */
        private inline fun numberOfBitmaps(start: Int, end: Int)
                = (end - (start and 0x7FFF_FFE0) + 0x1F) shr 5

        fun build(): SparseBitSet {
            if (ends.last() > MAX_CAPACITY) {
                throw RuntimeException("The maximum capacity exceeded.")
            }

            val indicies = IntArray(numOfIndices())
            val bitmaps = IntArray(numOfSlots() * 0x08)

            var zeroSlot = -1
            var nonZeroPageEnd = 0
            var currentSlot = 0

            for (i in 0 until starts.size) {
                val start = starts[i]  // inclusive
                val end = ends[i]  // exclusive

                val startPage = page(start)  // inclusive
                val endPage = page(end - 1)  // inclusive

                // Fills gap with zero slots
                if (startPage >= nonZeroPageEnd) {
                    if (startPage > nonZeroPageEnd) {
                        if (zeroSlot == -1) {
                            zeroSlot = currentSlot++
                        }
                        for (j in nonZeroPageEnd until startPage) {
                            indicies[zeroSlot]
                        }
                    }
                    indicies[startPage] = currentSlot++
                }

                // Set ones the given range in the bitmaps
                val startBitmapIndex = slotToBitmapStart(currentSlot - 1) + bitmapIndexInSlot(start)
                val numBitmaps = numberOfBitmaps(start, end)

                val startBitOffset = bitOffsetInBitmap(start)
                val endBitOffset = bitOffsetInBitmap(end)
                if (numBitmaps == 1) {
                    bitmaps[startBitmapIndex] = bitmaps[startBitmapIndex] or
                            ((FULLBIT_INT ushr startBitOffset) and (FULLBIT_INT shl (32 - endBitOffset)))
                } else {
                    bitmaps[startBitmapIndex] = bitmaps[startBitmapIndex] or
                            (FULLBIT_INT ushr startBitOffset)
                    for (j in 1 until numBitmaps - 1) {
                        bitmaps[startBitmapIndex + j] = FULLBIT_INT
                    }
                    bitmaps[startBitmapIndex + numBitmaps - 1] =
                        bitmaps[startBitmapIndex + numBitmaps - 1] or (FULLBIT_INT shl (32 - endBitOffset))
                }

                for (j in startPage + 1 until endPage + 1) {
                    indicies[j] = currentSlot++
                }

                nonZeroPageEnd = endPage + 1
            }

            return SparseBitSet(indicies, bitmaps, ends.last())
        }
    }

    operator fun contains(element: Int): Boolean {
        if (element >= maxValue) {
            return false
        }
        val startBitmapIndex = indices[element ushr 8] * 0x08
        val bitmapIndexInSlot = (element and 0xFF) ushr 5
        val bitOffsetInBitmap = element and 0x1f
        return (bitmaps[startBitmapIndex + bitmapIndexInSlot]
                and (TOP_BIT_INT ushr bitOffsetInBitmap)) != 0
    }
}