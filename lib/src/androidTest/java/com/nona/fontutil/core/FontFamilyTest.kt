package com.nona.fontutil.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.test.R
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FontFamilyTest {
    @Test
    fun create_from_single() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context.assets, "fonts/Roboto-Regular.ttf").build()
        val family = FontFamily.Builder(arrayOf(font)).build()
        Truth.assertThat(family).isNotNull()
        Truth.assertThat('a'.toInt() in family).isTrue()
        Truth.assertThat(family.name).isEqualTo("Roboto")
    }

    @Test
    fun create_from_multiple() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font1 = Font.Builder(context.assets, "fonts/Roboto-Regular.ttf").build()
        val font2 = Font.Builder(context, R.font.roboto_bold).build()
        val family = FontFamily.Builder(arrayOf(font1, font2)).build()
        Truth.assertThat(family).isNotNull()
        Truth.assertThat('a'.toInt() in family).isTrue()
        Truth.assertThat(family.name).isEqualTo("Roboto")
    }

    @Test
    fun create_with_name() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val font = Font.Builder(context.assets, "fonts/Roboto-Regular.ttf").build()
        val family = FontFamily.Builder(arrayOf(font))
            .setName("Roboto Custom")
            .build()
        Truth.assertThat(family).isNotNull()
        Truth.assertThat(family.name).isEqualTo("Roboto Custom")
    }
}
