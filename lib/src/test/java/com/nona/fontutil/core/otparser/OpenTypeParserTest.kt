package com.nona.fontutil.core.otparser

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.test.TestUtil
import org.junit.Test

private const val ROBOTO_DIR_PREFIX = "roboto-fonts"

class OpenTypeParserTest {
    @Test
    fun `parse style`() {
        mapOf(
            "Roboto-Thin.ttf" to FontStyle(250, false),
            "Roboto-ThinItalic.ttf" to FontStyle(250, true),
            "Roboto-Light.ttf" to FontStyle(300, false),
            "Roboto-LightItalic.ttf" to FontStyle(300, true),
            "Roboto-Regular.ttf" to FontStyle(400, false),
            "Roboto-RegularItalic.ttf" to FontStyle(400, true),
            "Roboto-Medium.ttf" to FontStyle(500, false),
            "Roboto-MediumItalic.ttf" to FontStyle(500, true),
            "Roboto-Bold.ttf" to FontStyle(700, false),
            "Roboto-BoldItalic.ttf" to FontStyle(700, true),
            "Roboto-BlackItalic.ttf" to FontStyle(900, true),
            "Roboto-Black.ttf" to FontStyle(900, false)
        ).forEach { (fileName, expectStyle) ->
            val fontFile = TestUtil.getThirdPartyFile("$ROBOTO_DIR_PREFIX/$fileName")

            assertWithMessage("File must exists: ${fontFile.absolutePath}")
                .that(fontFile.exists())
                .isTrue()

            val buffer = IOUtil.mmap(fontFile)

            val style = OpenTypeParser(buffer).parseStyle()
            assertThat(style).isEqualTo(expectStyle)
        }
    }
}