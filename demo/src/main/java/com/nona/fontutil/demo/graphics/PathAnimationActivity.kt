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

class PathAnimationView : View {
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

    var progress: Float = 0f
        set(value) {
            updateRenderPath(progress)
            invalidate()
            field = value
        }
        get() = field

    val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        setDuration(2000)
        addUpdateListener {
            progress = it.getAnimatedValue() as Float
        }
    }
    val textSize = 250f
    val paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    val fillPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 100f, 100f, Color.RED, Color.BLUE, Shader.TileMode.MIRROR)
    }
    val onCurvePaint = Paint().apply { color = Color.RED }
    val offCurvePaint = Paint().apply { color = Color.BLUE }

    val pathMeasure = PathMeasure()

    val font = OpenType(File("/system/fonts/Roboto-Regular.ttf"), 0)
    val glyphs: List<Glyph> = font.shapeText("Android")
    val paths: List<Path> = glyphs.map { it.toPath(textSize) }
    val renderPaths = List(paths.size) { Path() }

    fun updateRenderPath(progress: Float) {
        val tmp = Path()
        paths.forEachIndexed { i, path ->
            pathMeasure.setPath(path, true)
            renderPaths[i].reset()
            do {
                val end = pathMeasure.length * progress
                tmp.reset()
                pathMeasure.getSegment(0f, end, tmp, true)
                renderPaths[i].addPath(tmp)
            } while (pathMeasure.nextContour())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        canvas.save()
        try {
            val scale = textSize / font.head.unitsPerEm

            canvas.translate(0f, font.hhea.ascent * scale) // baseline

            canvas.save()
            try {
                var totalAdvance = 0f
                for (i in glyphs.indices) {
                    val scale = textSize / glyphs[i].unitPerEm
                    canvas.translate(glyphs[i].lsb * scale, 0f)
                    canvas.drawPath(renderPaths[i], paint)
                    if (progress >= 0.8) {
                        canvas.drawPath(paths[i], fillPaint.apply {
                            alpha = (((progress - 0.8f) / 0.2) * 255f).toInt()
                        })
                    }
                    canvas.translate((glyphs[i].advance - glyphs[i].lsb) * scale, 0f)
                    totalAdvance += glyphs[i].advance * scale
                }

            } finally {
                canvas.restore()
            }

        } finally {
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        anim.start()
        return super.onTouchEvent(event)
    }
}

class PathAnimationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = PathAnimationView(this)
        setContentView(view)


    }
}