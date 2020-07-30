package com.nona.fontutil.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.nona.fontutil.core.otparser.OutlineGlyph
import com.nona.fontutil.demo.databinding.ActivityMainBinding
import com.nona.fontutil.graphics.OpenType
import java.io.File


class DebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ot = OpenType(File("/system/fonts/NotoSansCJK-Regular.ttc"), 1)
        val glyphId = ot.getGlyphId('あ'.toInt())
        val glyph = ot.getGlyph(glyphId) as OutlineGlyph
    }
}