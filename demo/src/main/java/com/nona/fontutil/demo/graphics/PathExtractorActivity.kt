package com.nona.fontutil.demo.graphics

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nona.fontutil.core.otparser.Glyph
import com.nona.fontutil.core.otparser.OutlineGlyph
import com.nona.fontutil.graphics.OpenType
import com.nona.fontutil.graphics.toPath
import java.io.File

class PathDrawView : View {
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    val textSize = 1000f
    val paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    val onCurvePaint = Paint().apply { color = Color.RED }
    val offCurvePaint = Paint().apply { color = Color.BLUE }

    val font = OpenType(File("/system/fonts/NotoSansCJK-Regular.ttc"), 1)
    val glyph = font.getGlyph(font.getGlyphId('ぬ'.toInt())) as OutlineGlyph
    val path = glyph.toPath(textSize)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        canvas.save()
        try {
            val scale = textSize / font.head.unitsPerEm
            canvas.translate(0f, font.hhea.ascent * scale) // baseline

            canvas.drawPath(path, paint)

            glyph.contours.forEach { contour ->
                contour.forEach { x, y, onCurve ->
                    canvas.drawCircle(
                        x * scale,
                        -y * scale,
                        8f,
                        if (onCurve) onCurvePaint else offCurvePaint
                    )
                }
            }
        } finally {
            canvas.restore()
        }
    }
}

class PathExtractorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = PathDrawView(this)
        setContentView(view)


    }
}