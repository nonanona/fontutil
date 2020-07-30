package com.nona.fontutil.core.otparser

enum class OutlineType {
    QUADRATIC_BEZIER_CURVE,
    CUBIC_BEZIER_CURVE
}

sealed class Glyph(
    val unitPerEm: Int
)

class Contour(internal val points: List<Long>) {
    val size: Int get() = points.size

    fun getXCoord(i: Int): Int = xPos(points[i])
    fun getYCoord(i: Int): Int = yPos(points[i])
    fun isOnCurve(i: Int): Boolean = onCurvePoint(points[i])

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
    val type: OutlineType,
    val contours: List<Contour>,
    unitPerEm: Int
) : Glyph(unitPerEm)

internal fun setXPos(packed: Long, x: Int) =
    (packed and 0xFF_0000_FFFFL) or ((x.toLong() and 0xFFFFL) shl 16)
internal fun setYPos(packed: Long, y: Int) =
    (packed and 0xFF_FFFF_0000L) or (y.toLong() and 0xFFFFL)
internal fun setFlag(packed: Long, flag: Int) =
    (packed and 0x00_FFFF_FFFFL) or (flag.toLong() shl 32)
internal fun pack(x: Int, y: Int, flag: Int) =
    (flag.toLong() shl 32) or ((x.toLong() and 0xFFFFL) shl 16) or ((y.toLong() and 0xFFFFL))

internal fun xPos(packed: Long) = ((packed and 0xFFFF_0000L) shr 16).toShort().toInt()
internal fun yPos(packed: Long) = ((packed and 0x0000_FFFFL)).toShort().toInt()
internal fun onCurvePoint(packed: Long) = (packed and 0x01_0000_0000L) == 0x01_0000_0000L
internal fun xShortVector(packed: Long) = (packed and 0x02_0000_0000L) == 0x02_0000_0000L
internal fun yShortVector(packed: Long) = (packed and 0x04_0000_0000L) == 0x04_0000_0000L
internal fun xIsSameOrPositiveXShortVector(packed: Long) =
    (packed and 0x10_0000_0000L) == 0x10_0000_0000L
internal fun yIsSameOrPositiveYShortVector(packed: Long) =
    (packed and 0x20_0000_0000L) == 0x20_0000_0000L

class ContourBuilder {
    val contours = mutableListOf<Contour>()
    var points = mutableListOf<Long>()

    fun addContour() {
        if (points.isNotEmpty()) {
            contours.add(Contour(points))
        }
        points = mutableListOf<Long>()
    }

    fun addPoint(x: Int, y:Int, onCurve: Boolean) {
        points.add(pack(x, y, if (onCurve) 1 else 0))
    }

    fun build(): List<Contour> {
        contours.add(Contour(points))
        return contours
    }
}