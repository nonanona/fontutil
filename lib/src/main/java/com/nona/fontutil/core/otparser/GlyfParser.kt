package com.nona.fontutil.core.otparser

import android.util.Log
import com.nona.fontutil.base.int16
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint8
import java.nio.ByteBuffer

object GlyfParser {

    fun getGlyph(buffer: ByteBuffer, glyphOffset: Long, unitPerEm: Int): Glyph {
        buffer.position(glyphOffset)
        val numberOfContents = buffer.int16()
        if (numberOfContents < 0) return getCompositeGlyph(buffer)

        buffer.int16()
        buffer.int16()
        buffer.int16()
        buffer.int16()

        val endPtsOfContours = IntArray(numberOfContents)
        for (i in 0 until numberOfContents) {
            endPtsOfContours[i] = buffer.int16()
        }

        val instructionLength = buffer.int16()
        buffer.position(buffer.position() + instructionLength) // skip instructions

        val points = LongArray(endPtsOfContours.last() + 1)  // end pts is inclusive

        var index = 0
        while (index < points.size) {  // EndPts is inclusive
            val flag = buffer.uint8()
            points[index] = setFlag(points[index], flag)
            index++

            if ((flag and 0x08) == 0x08) { // REPEAT_FLAG
                val repeatCount = buffer.uint8()
                for (i in 0 until repeatCount) {
                    points[index] = setFlag(points[index], flag)
                    index++
                }
            }
        }

        var currentX = 0
        points.forEachIndexed { index, value ->
            currentX = if (xShortVector(value)) {
                if (xIsSameOrPositiveXShortVector(value)) {
                    currentX + buffer.uint8().toShort()
                } else {
                    currentX - buffer.uint8().toShort()
                }
            } else {
                if (xIsSameOrPositiveXShortVector(value)) {
                    currentX
                } else {
                    currentX + buffer.int16().toShort()
                }
            }
            points[index] = setXPos(points[index], currentX)
        }

        var currentY = 0
        points.forEachIndexed { index, value ->
            currentY = if (yShortVector(value)) {
                if (yIsSameOrPositiveYShortVector(value)) {
                    currentY + buffer.uint8().toShort()
                } else {
                    currentY - buffer.uint8().toShort()
                }
            } else {
                if (yIsSameOrPositiveYShortVector(value)) {
                    currentY
                } else {
                    currentY + buffer.int16().toShort()
                }
            }
            points[index] = setYPos(points[index], currentY)
        }

        var prevContStart = 0
        val contours = endPtsOfContours.map {
            val res = Contour(
                points.slice(prevContStart .. it)
            )
            prevContStart = it + 1  // end pt is inclusive
            res
        }

        return OutlineGlyph(
            type = OutlineType.QUADRATIC_BEZIER_CURVE,
            contours = contours,
            unitPerEm = unitPerEm)

    }

    private fun getCompositeGlyph(buffer: ByteBuffer): Glyph {
        TODO("Composite Glyph is not yet implemented")
    }
}