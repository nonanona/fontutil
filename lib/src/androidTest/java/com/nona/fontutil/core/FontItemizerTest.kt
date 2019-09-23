package com.nona.fontutil.core

import android.graphics.Typeface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.test.R
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FontItemizerTest {
    @Test
    fun itemize() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collection = FontCollection(arrayOf(
            FontFamily.Builder(arrayOf(
                Font.Builder(context.assets, "fonts/roboto-font/Roboto-Regular.ttf").build()
            )).build()),
            Typeface.SANS_SERIF
        )

        val res = FontItemizer(collection).itemize("Hello, World.")
        assertThat(res).isNotEmpty()
        assertThat(res[0].length).isEqualTo("Hello, World.".length)
        assertThat(res[0].family!!.name).isEqualTo("Roboto")
    }

    @Test
    fun itemize_with_unknowns() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collection = FontCollection(arrayOf(
            FontFamily.Builder(arrayOf(
                Font.Builder(context.assets, "fonts/roboto-font/Roboto-Regular.ttf").build()
            )).build()),
            Typeface.SANS_SERIF
        )

        val res = FontItemizer(collection).itemize("Hello, 世界.")
        assertThat(res.size).isEqualTo(3)
        assertThat(res[0].length).isEqualTo("Hello, ".length)
        assertThat(res[0].family!!.name).isEqualTo("Roboto")
        assertThat(res[1].length).isEqualTo("世界".length)
        assertThat(res[1].family).isNull()
        assertThat(res[2].length).isEqualTo(".".length)
        assertThat(res[2].family!!.name).isEqualTo("Roboto")
    }

    @Test
    fun itemize_with_multifont() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collection = FontCollection(arrayOf(
            FontFamily.Builder(arrayOf(
                Font.Builder(context.assets, "fonts/roboto-font/Roboto-Regular.ttf").build()
            )).build(),
            FontFamily.Builder(arrayOf(
                Font.Builder(context.assets, "fonts/noto-cjk/NotoSerifCJK-Regular.ttc").build()
            )).build()),
            Typeface.SANS_SERIF
        )

        val res = FontItemizer(collection).itemize("Hello, 世界.")
        assertThat(res.size).isEqualTo(2)
        assertThat(res[0].length).isEqualTo("Hello, ".length)
        assertThat(res[0].family!!.name).isEqualTo("Roboto")
        assertThat(res[1].length).isEqualTo("世界.".length)
        assertThat(res[1].family!!.name).isEqualTo("Noto Serif CJK JP")
    }
}
