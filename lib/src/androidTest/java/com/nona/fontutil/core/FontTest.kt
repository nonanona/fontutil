package com.nona.fontutil.core

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.test.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FontTest {
    @Test
    fun create_from_asset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context.assets, "fonts/Roboto-Regular.ttf").build()
        assertThat(font).isNotNull()
        assertThat(font.style).isEqualTo(FontStyle(400, false))
    }

    @Test
    fun create_from_resource() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context, R.font.roboto_regular).build()
        assertThat(font).isNotNull()
        assertThat(font.style).isEqualTo(FontStyle(400, false))

    }

    @Test
    fun create_from_asset_override_style() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context.assets, "fonts/Roboto-Regular.ttf")
            .setStyle(FontStyle(700, true))
            .build()
        assertThat(font).isNotNull()
        assertThat(font.style).isEqualTo(FontStyle(700, true))
    }

    @Test
    fun create_from_resource_override_style() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context, R.font.roboto_regular)
            .setStyle(FontStyle(700, true))
            .build()
        assertThat(font).isNotNull()
        assertThat(font.style).isEqualTo(FontStyle(700, true))

    }
}
