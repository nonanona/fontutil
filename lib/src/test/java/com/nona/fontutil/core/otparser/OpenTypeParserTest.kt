package com.nona.fontutil.core.otparser

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.graphics.OpenType
import com.nona.fontutil.test.TestUtil
import org.junit.Test
import java.io.File

private const val ROBOTO_DIR_PREFIX = "roboto-fonts"
private const val MANSALVA_DIR_PREFIX = "mansalva"
private const val NOTO_CJK_DIR_PREFIX = "noto-cjk"

class OpenTypeParserTest {
    val robotoFontsStyles = mapOf(
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
    )

    val robotoFontsNames = mapOf(
        "Roboto-Thin.ttf" to NameRecord("Roboto", "Thin"),
        "Roboto-ThinItalic.ttf" to NameRecord("Roboto", "Thin Italic"),
        "Roboto-Light.ttf" to NameRecord("Roboto", "Light"),
        "Roboto-LightItalic.ttf" to NameRecord("Roboto", "Light Italic"),
        "Roboto-Regular.ttf" to NameRecord("Roboto", "Regular"),
        "Roboto-RegularItalic.ttf" to NameRecord("Roboto", "Italic"),
        "Roboto-Medium.ttf" to NameRecord("Roboto", "Medium"),
        "Roboto-MediumItalic.ttf" to NameRecord("Roboto", "Medium Italic"),
        "Roboto-Bold.ttf" to NameRecord("Roboto", "Bold"),
        "Roboto-BoldItalic.ttf" to NameRecord("Roboto", "Bold Italic"),
        "Roboto-Black.ttf" to NameRecord("Roboto", "Black"),
        "Roboto-BlackItalic.ttf" to NameRecord("Roboto", "Black Italic")
        )

    @Test
    fun `parse style`() {
        robotoFontsStyles.forEach { (fileName, expectStyle) ->
            val fontFile = TestUtil.getThirdPartyFile("$ROBOTO_DIR_PREFIX/$fileName")

            assertWithMessage("File must exists: ${fontFile.absolutePath}")
                .that(fontFile.exists())
                .isTrue()

            val buffer = IOUtil.mmap(fontFile)

            val style = OpenTypeParser(buffer).parseStyle()
            assertThat(style).isEqualTo(expectStyle)
        }
    }

    @Test
    fun `parse cmap12`() {
        val fontFile = TestUtil.getThirdPartyFile("$ROBOTO_DIR_PREFIX/Roboto-Regular.ttf")

        assertWithMessage("File must exists: ${fontFile.absolutePath}")
            .that(fontFile.exists())
            .isTrue()

        val buffer = IOUtil.mmap(fontFile)

        val coverage = OpenTypeParser(buffer).parseCoverage()

        for (i in 0x0000 until 0x10FFFF) {
            if (i in ROBOTO_COVERAGE) {
                assertWithMessage("U+${String.format("%04X", i)} must be supported")
                    .that(i in coverage)
                    .isTrue()
            } else {
                assertWithMessage("U+${String.format("%04X", i)} must not be supported")
                    .that(i in coverage)
                    .isFalse()
            }
        }

    }

    @Test
    fun `parse cmap4`() {
        val fontFile = TestUtil.getThirdPartyFile("$MANSALVA_DIR_PREFIX/Mansalva-Regular.ttf")

        assertWithMessage("File must exists: ${fontFile.absolutePath}")
            .that(fontFile.exists())
            .isTrue()

        val buffer = IOUtil.mmap(fontFile)

        val coverage = OpenTypeParser(buffer).parseCoverage()

        for (i in 0x0000 until 0x10FFFF) {
            if (i in MANSALVA_COVERAGE) {
                assertWithMessage("U+${String.format("%04X", i)} must be supported")
                    .that(i in coverage)
                    .isTrue()
            } else {
                assertWithMessage("U+${String.format("%04X", i)} must not be supported")
                    .that(i in coverage)
                    .isFalse()
            }
        }

    }

    @Test
    fun `parse name`() {
        robotoFontsNames.forEach { (fileName, nameRecord) ->
            val fontFile = TestUtil.getThirdPartyFile("$ROBOTO_DIR_PREFIX/$fileName")

            assertWithMessage("File must exists: ${fontFile.absolutePath}")
                .that(fontFile.exists())
                .isTrue()

            val buffer = IOUtil.mmap(fontFile)

            val name = OpenTypeParser(buffer).parseName()
            assertThat(name).isEqualTo(nameRecord)
        }
    }

    @Test
    fun `parce name (TTC)`() {
        val fontFile = TestUtil.getThirdPartyFile("noto-cjk/NotoSerifCJK-Regular.ttc")

        assertWithMessage("File must exists: ${fontFile.absolutePath}")
            .that(fontFile.exists())
            .isTrue()

        val buffer = IOUtil.mmap(fontFile)
        assertThat(OpenTypeParser(buffer).parseName())
            .isEqualTo(NameRecord("Noto Serif CJK JP", "Regular"))

        assertThat(OpenTypeParser(buffer, 0).parseName())
            .isEqualTo(NameRecord("Noto Serif CJK JP", "Regular"))

        assertThat(OpenTypeParser(buffer, 1).parseName())
            .isEqualTo(NameRecord("Noto Serif CJK KR", "Regular"))

        assertThat(OpenTypeParser(buffer, 2).parseName())
            .isEqualTo(NameRecord("Noto Serif CJK SC", "Regular"))

        assertThat(OpenTypeParser(buffer, 3).parseName())
            .isEqualTo(NameRecord("Noto Serif CJK TC", "Regular"))
    }

    @Test
    fun `getGlyphId`() {
        //val fontFile = TestUtil.getThirdPartyFile("$NOTO_CJK_DIR_PREFIX/NotoSerifCJK-Regular.ttc")
        val fontFile = File("/home/nona/NotoSansCJK-Regular.ttc")

        assertWithMessage("File must exists: ${fontFile.absolutePath}")
            .that(fontFile.exists())
            .isTrue()

        val ot = OpenType(fontFile, 1)
        val r = ot.getGlyphId('E'.toInt())
        ot.getGlyph(r)
    }
}