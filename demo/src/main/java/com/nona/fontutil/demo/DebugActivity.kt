package com.nona.fontutil.demo

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.core.FontFamily
import com.nona.fontutil.span.SpanProcessor
import kotlinx.android.synthetic.main.activity_main.*

class DebugActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mansalva = FontFamily.Builder(
            arrayOf(Font.Builder(assets, "mansalva/Mansalva-Regular.ttf").build())
        ).build()
        val kosugiMaru = FontFamily.Builder(
            arrayOf(Font.Builder(assets, "kosugi-maru/KosugiMaru-Regular.ttf").build())
        ).build()

        val collection = FontCollection(arrayOf(mansalva, kosugiMaru), Typeface.SERIF)

        textView.text = SpanProcessor.process("こんにちは、 World", collection)
    }
}