package com.nona.fontutil.demo

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import com.nona.fontutil.assets.AssetsXMLParser
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.core.FontFamily
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.provider.FontFetcher
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

        val collection = AssetsXMLParser.parseFontCollectionXml(this, "font/roboto.xml")
        */


        val font = FontFetcher(this, "com.google.android.gms.fonts")
            .fetchSingleFont("Advent Pro", FontStyle(400, false))
        if (font == null) {
            Log.e("MainActivity", "Failed to fetch Advent Pro")
            return
        }

        val adventPro = FontFamily.Builder(arrayOf(font)).build()
        val collection = FontCollection(arrayOf(adventPro), Typeface.SERIF)
        textView.text = SpanProcessor.process("こんにちは、 World", collection)

    }
}