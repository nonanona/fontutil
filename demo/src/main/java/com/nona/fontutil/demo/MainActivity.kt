package com.nona.fontutil.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nona.fontutil.core.Font

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            var font = Font.Builder(this@MainActivity, it)
        }

    }
}
