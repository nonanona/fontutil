package com.nona.fontutil.demo

import android.app.Activity
import android.os.Bundle
import com.nona.fontutil.assets.AssetsXMLParser
import com.nona.fontutil.assets.CustomTagParserManager
import com.nona.fontutil.google.GoogleFontTagParser
import com.nona.fontutil.span.SpanProcessor
import kotlinx.android.synthetic.main.activity_main.*

class DebugActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        val mansalva = FontFamily.Builder(
            arrayOf(Font.Builder(assets, "mansalva/Mansalva-Regular.ttf").build())
        ).build()
        val kosugiMaru = FontFamily.Builder(
            arrayOf(Font.Builder(assets, "kosugi-maru/KosugiMaru-Regular.ttf").build())
        ).build()
        */

        CustomTagParserManager.register("Google", GoogleFontTagParser(this))
        val collection = AssetsXMLParser.parseFontCollectionXml(this, "font/roboto.xml")

        textView.text = SpanProcessor.process("こんにちは、 World", collection)

    }
}