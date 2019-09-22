package com.nona.fontutil.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nona.fontutil.core.otparser.OpenTypeParser
import com.nona.fontutil.base.IOUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assets.list("roboto").forEach {
            android.util.Log.e("MainActivity", "$it")

            val buffer = IOUtil.mmap(assets, "roboto/$it")
            val style = OpenTypeParser(buffer).parseStyle()
            android.util.Log.e("MainActivity", "  Style: $style")
        }

        arrayOf(
            R.font.roboto_black,
            R.font.roboto_black_italic,
            R.font.roboto_bold,
            R.font.roboto_bold_italic,
            R.font.roboto_light,
            R.font.roboto_light_italic,
            R.font.roboto_medium,
            R.font.roboto_medium_italic,
            R.font.roboto_regular,
            R.font.roboto_regular_italic,
            R.font.roboto_thin,
            R.font.roboto_thin_italic
        ).forEach {
            val buffer = IOUtil.mmap(resources, it)
            val style = OpenTypeParser(buffer).parseStyle()
            android.util.Log.e("MainActivity", "  Style: $style")
        }

    }
}
