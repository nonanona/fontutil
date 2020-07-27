package com.nona.fontutil.demo.graphics

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nona.fontutil.core.otparser.Glyph
import com.nona.fontutil.core.otparser.OutlineGlyph
import com.nona.fontutil.graphics.OpenType
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
    var path: Path? = null
    val paint = Paint().apply { color = Color.GRAY }
    var glyph: OutlineGlyph? = null
    val onCurvePaint = Paint().apply { color = Color.RED }
    val offCurvePaint = Paint().apply { color = Color.BLUE }

    init {
        val ot = OpenType(context.assets, "kosugi-maru/KosugiMaru-Regular.ttf", 1)
        val glyphId = ot.getGlyphId('鬱'.toInt())
        path = ot.getGlyphPath(glyphId, textSize)
        glyph = ot.getGlyph(glyphId) as OutlineGlyph
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        try {
            canvas.translate(0f, canvas.height * 2f / 3f)

            path?.let {
                canvas.drawPath(it, paint)
            }

            glyph?.let{
                val scale = textSize / it.unitPerEm
                it.contours.forEach { contour ->
                    contour.forEach { x, y, onCurve ->
                        canvas.drawCircle(
                            x * scale,
                            -y * scale,
                            8f,
                            if (onCurve) onCurvePaint else offCurvePaint
                        )
                    }
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
        setContentView(PathDrawView(this))
    }
}