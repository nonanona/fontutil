package com.nona.fontutil.core.otparser

import android.util.Log
import com.nona.fontutil.base.int16
import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint8
import java.nio.ByteBuffer

sealed class Glyph(
    val unitPerEm: Int,
    val xMin: Int,
    val yMin: Int,
    val xMax: Int,
    val yMax: Int
)

class Contour(private val points: List<Long>) {
    val size: Int get() = points.size

    fun getXCoord(i: Int): Int = xPos(points[i])
    fun getYCoord(i: Int): Int = yPos(points[i])
    fun isOnCoord(i: Int): Boolean = onCurvePoint(points[i])

    fun forEach(f: (Int, Int, Boolean) -> Unit) {
        points.forEach {
            f(xPos(it), yPos(it), onCurvePoint(it))
        }
    }

    fun forEachIndexed(f: (Int, Int, Int, Boolean) -> Unit) {
        points.forEachIndexed { index, it ->
            f(index, xPos(it), yPos(it), onCurvePoint(it))
        }
    }
}

class OutlineGlyph(
    val contours: List<Contour>,
    unitPerEm: Int,
    xMin: Int,
    yMin: Int,
    xMax: Int,
    yMax: Int
) : Glyph(unitPerEm, xMin, yMin, xMax, yMax)

private fun setXPos(packed: Long, x: Int) =
    (packed and 0xFF_0000_FFFFL) or ((x.toLong() and 0xFFFFL) shl 16)
private fun setYPos(packed: Long, y: Int) =
    (packed and 0xFF_FFFF_0000L) or (y.toLong() and 0xFFFFL)
private fun setFlag(packed: Long, flag: Int) =
    (packed and 0x00_FFFF_FFFFL) or (flag.toLong() shl 32)

private fun xPos(packed: Long) = ((packed and 0xFFFF_0000L) shr 16).toShort().toInt()
private fun yPos(packed: Long) = ((packed and 0x0000_FFFFL)).toShort().toInt()
private fun onCurvePoint(packed: Long) = (packed and 0x01_0000_0000L) == 0x01_0000_0000L
private fun xShortVector(packed: Long) = (packed and 0x02_0000_0000L) == 0x02_0000_0000L
private fun yShortVector(packed: Long) = (packed and 0x04_0000_0000L) == 0x04_0000_0000L
private fun xIsSameOrPositiveXShortVector(packed: Long) =
    (packed and 0x10_0000_0000L) == 0x10_0000_0000L
private fun yIsSameOrPositiveYShortVector(packed: Long) =
    (packed and 0x20_0000_0000L) == 0x20_0000_0000L

object GlyfParser {

    fun getGlyph(buffer: ByteBuffer, glyphOffset: Long, unitPerEm: Int): Glyph {
        buffer.position(glyphOffset)
        val numberOfContents = buffer.int16()
        if (numberOfContents < 0) return getCompositeGlyph(buffer)

        val xMin = buffer.int16()
        val yMin = buffer.int16()
        val xMax = buffer.int16()
        val yMax = buffer.int16()

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
            contours = contours,
            unitPerEm = unitPerEm,
            xMin = xMin,
            yMin = yMin,
            xMax = xMax,
            yMax = yMax)

    }

    private fun getCompositeGlyph(buffer: ByteBuffer): Glyph {
        TODO("Composite Glyph is not yet implemented")
    }
}