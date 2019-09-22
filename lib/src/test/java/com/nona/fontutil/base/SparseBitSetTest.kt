package com.nona.fontutil.base

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import kotlin.random.Random

class SparseBitSetTest {
    @Test
    fun `simple test`() {
        val sbs = SparseBitSet.Builder()
            .append(0x1234)
            .build()

        assertThat(sbs.contains(0x1233)).isFalse()
        assertThat(sbs.contains(0x1234)).isTrue()
        assertThat(sbs.contains(0x1235)).isFalse()
    }

    @Test
    fun `stress test`() {
        val random = Random(3141592)

        val builder = SparseBitSet.Builder()
        val dataInSet = mutableSetOf<Int>()

        var cur = 0
        for (i in 0 until 1000) {
            cur += random.nextInt(30)
            val start = cur
            cur += random.nextInt(30)
            val end = cur
            builder.append(start, end)

            for (j in start until end) {
                dataInSet.add(j)
            }
        }

        val sbs = builder.build()

        for (i in 0 .. dataInSet.max()!!) {
            assertWithMessage("$i : ${i in sbs}, ${i in dataInSet}")
                .that(i in sbs)
                .isEqualTo(i in dataInSet)
        }
    }
}