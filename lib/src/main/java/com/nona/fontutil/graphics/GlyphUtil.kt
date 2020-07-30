package com.nona.fontutil.graphics

import android.graphics.Path
import android.util.Log
import com.nona.fontutil.core.otparser.Contour
import com.nona.fontutil.core.otparser.Glyph
import com.nona.fontutil.core.otparser.OutlineGlyph
import com.nona.fontutil.core.otparser.OutlineType

internal fun Glyph.toPath(unitPerEm: Int, textSize: Float): Path {
    return when (this) {
        is OutlineGlyph -> OutlineGlyphToPath(this, unitPerEm, textSize)
        else -> TODO("Not yet implemented")
    }
}

private fun quadBezierParser(
    c: Contour,
    scale: Float,
    quadTo: (Float, Float, Float, Float) -> Unit,
    lineTo: (Float, Float) -> Unit,
    moveTo: (Float, Float) -> Unit
) {
    moveTo(c.getXCoord(0) * scale, - c.getYCoord(0) * scale)

    for (i in 1 .. c.size) {
        val curIndex = i % c.size
        val prevIndex = (i - 1) % c.size

        if (c.isOnCurve(prevIndex)) {
            if (c.isOnCurve(curIndex)) {
                lineTo(
                    c.getXCoord(curIndex) * scale,
                    -c.getYCoord(curIndex) * scale)
            } else {
                // skip.
            }
        } else {
            if (c.isOnCurve(curIndex)) {
                quadTo(
                    c.getXCoord(prevIndex) * scale,
                    -c.getYCoord(prevIndex) * scale,
                    c.getXCoord(curIndex) * scale,
                    -c.getYCoord(curIndex) * scale
                )
            } else {
                val prevX = c.getXCoord(prevIndex) * scale
                val prevY = -c.getYCoord(prevIndex) * scale
                val curX =  c.getXCoord(curIndex) * scale
                val curY = - c.getYCoord(curIndex) * scale
                quadTo(prevX, prevY, (prevX + curX) / 2f, (prevY + curY) / 2f)
            }
        }
    }
}

private fun cubicBezierParser(
    c: Contour,
    scale: Float,
    cubicTo: (Float, Float, Float, Float, Float, Float) -> Unit,
    lineTo: (Float, Float) -> Unit,
    moveTo: (Float, Float) -> Unit
) {
    if (c.size == 0) return
    moveTo(c.getXCoord(0) * scale, - c.getYCoord(0) * scale)

    for (i in 1 .. c.size) {
        val curIndex = i % c.size
        if (!c.isOnCurve(curIndex)) continue

        val prevIndex = (i - 1) % c.size
        if (c.isOnCurve(prevIndex)) {
            lineTo(
                c.getXCoord(curIndex) * scale,
                -c.getYCoord(curIndex) * scale)
        } else {
            val prevPrevIndex = (i - 2) % c.size
            if (c.isOnCurve(prevPrevIndex)) { throw RuntimeException("Not a Cubic Bezier Curve") }

            cubicTo(
                c.getXCoord(prevPrevIndex) * scale,
                -c.getYCoord(prevPrevIndex) * scale,
                c.getXCoord(prevIndex) * scale,
                -c.getYCoord(prevIndex) * scale,
                c.getXCoord(curIndex) * scale,
                -c.getYCoord(curIndex) * scale
            )
        }
    }
}

private fun OutlineGlyphToPath(glyph: OutlineGlyph, unitPerEm: Int, textSize: Float): Path {
    val res = Path()
    val scale = textSize / unitPerEm
    if (glyph.type == OutlineType.QUADRATIC_BEZIER_CURVE) {
        glyph.contours.forEach { contour ->
            val path = Path()
            quadBezierParser(contour,
                scale,
                quadTo = { x1, y1, x2, y2 -> path.quadTo(x1, y1, x2, y2) },
                lineTo = { x, y -> path.lineTo(x, y) },
                moveTo = { x, y -> path.moveTo(x, y) }
            )
            res.addPath(path)
        }
    } else {
        glyph.contours.forEach { contour ->
            val path = Path()
            cubicBezierParser(contour,
                scale,
                cubicTo = { x1, y1, x2, y2, x3, y3-> path.cubicTo(x1, y1, x2, y2, x3, y3) },
                lineTo = { x, y -> path.lineTo(x, y) },
                moveTo = { x, y -> path.moveTo(x, y) }
            )
            res.addPath(path)
        }

    }
    return res
}