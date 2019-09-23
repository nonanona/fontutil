package com.nona.fontutil.demo

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.core.FontFamily
import com.nona.fontutil.core.FontItemizer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val robotoFamily = FontFamily.Builder(
            arrayOf(Font.Builder(this, R.font.roboto_regular).build())
        ).build()

        val notoCJKFamily = FontFamily.Builder(
            arrayOf(Font.Builder(this, R.font.noto_cjk).build())
        ).build()

        val collection = FontCollection(
            arrayOf(
                robotoFamily,
                notoCJKFamily
            ),
            Typeface.SANS_SERIF
        )

        val itemizer = FontItemizer(collection)
        val r = itemizer.itemize("Hello, 世界.")

        var prev = 0
        for (run in r) {
            android.util.Log.e(
                "MainActivity",
                "($prev, ${prev + run.length}) -> ${run.family?.name ?: "System"}")

            prev += run.length
        }
    }
}
