package com.nona.fontutil.graphics

import android.graphics.Path
import android.util.Log
import com.nona.fontutil.core.otparser.Glyph
import com.nona.fontutil.core.otparser.OutlineGlyph

internal fun Glyph.toPath(unitPerEm: Int, textSize: Float): Path {
    return when (this) {
        is OutlineGlyph -> OutlineGlyphToPath(this, unitPerEm, textSize)
        else -> TODO("Not yet implemented")
    }
}

private fun setCurve(
    path: Path,
    x: Float, y: Float, onCurve: Boolean,
    prevX: Float, prevY: Float, prevOnCurve: Boolean
) {
    if (prevOnCurve) {
        if (onCurve) {  // prev ON, cur ON
            path.lineTo(x, y)
        } else { // prev ON, cur OFF
            // skip. Will set curve on next point.
        }
    } else {
        if (onCurve) { // prev OFF, cur ON
            path.quadTo(prevX, prevY, x, y)
        } else { // prev OFF, cur OFF
            val mx = (prevX + x) / 2f
            val my = (prevY + y) / 2f
            path.quadTo(prevX, prevY, mx, my)
        }
    }
}

private fun OutlineGlyphToPath(glyph: OutlineGlyph, unitPerEm: Int, textSize: Float): Path {
    val res = Path()
    val scale = textSize / unitPerEm
    glyph.contours.forEach { contour ->
        val path = Path()
        var prevX = 0f
        var prevY = 0f
        var prevOnCurve = false
        contour.forEachIndexed { index, xPt, yPt, onCurve ->
            Log.e("Debug", "  $xPt, $yPt $onCurve")
            val x = xPt * scale
            val y = - yPt * scale
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                setCurve(path, x, y, onCurve, prevX, prevY, prevOnCurve)
            }

            prevX = x
            prevY = y
            prevOnCurve = onCurve
        }
        setCurve(path,
            contour.getXCoord(0) * scale,
            - contour.getYCoord(0) * scale,
            contour.isOnCoord(0),
            prevX,
            prevY,
            prevOnCurve
        )
        res.addPath(path)
    }
    return res
}