package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint8
import java.nio.ByteBuffer

private const val HSTEM = 1
private const val VSTEM = 3
private const val VMOVETO = 4
private const val RLINETO = 5
private const val HLINETO = 6
private const val VLINETO = 7
private const val RRCURVETO = 8
private const val CALLSUBR = 10
private const val RETURN = 11
private const val ENDCHAR = 14
private const val HSTEMHM = 18
private const val HINTMASK = 19
private const val CNTMASK = 20
private const val RMOVETO = 21
private const val HMOVETO = 22
private const val VSTEMHM = 23
private const val RCURVELINE = 24
private const val RLINECURVE = 25
private const val VVCURVETO = 26
private const val HHCURVETO = 27
private const val SHORTINT = 28
private const val CALLGSUBR = 29
private const val VHCURVETO = 30
private const val HVCURVETO = 31
private const val AND = 1203
private const val OR = 1204
private const val NOT = 1205
private const val ABS = 1209
private const val ADD = 1210
private const val SUB = 1211
private const val DIV = 1212
private const val NEG = 1214
private const val EQ = 1215
private const val DROP = 1218
private const val PUT = 1220
private const val GET = 1221
private const val IFELSE = 1222
private const val RANDOM = 1223
private const val MUL = 1224
private const val SQRT = 1226
private const val DUP = 1227
private const val EXCH = 1228
private const val INDEX = 1229
private const val ROLL = 1230
private const val HFLEX = 1234
private const val FLEX = 1235
private const val HFLEX1 = 1236
private const val FLEX1 = 1237

data class VMContext(
    var buffer: ByteBuffer,
    var x: Int = 0,
    var y: Int = 0,
    var cff: CFFMetadata,
    var fd: FDMetadata,
    var numHints: Int = 0,
    var builder: ContourBuilder,
    val args: MutableList<Int> = mutableListOf()
)

// true on continue, false on endchar
fun exec(ctx: VMContext, startPc: Long, endPc: Long): Boolean {
    val value = Operation()
    val buffer = ctx.buffer
    val builder = ctx.builder
    val args = ctx.args
    buffer.position(startPc)
    while (buffer.position() < endPc) {
        parseCharString(buffer, value)
        when (value.type) {
            OperationType.INTEGER -> { args.add(value.intValue) }
            OperationType.OPERATOR -> {
                when (value.intValue) {
                    ////////////////////////////////////////////////////////////////////////////////
                    // Hint Ops
                    HSTEM -> {
                        ctx.numHints += args.size / 2
                        args.clear()
                    }
                    VSTEM -> {
                        ctx.numHints += args.size / 2
                        args.clear()
                    }
                    HSTEMHM -> {
                        ctx.numHints += args.size / 2
                        args.clear()
                    }
                    VSTEMHM -> {
                        ctx.numHints += args.size / 2
                        args.clear()
                    }
                    HINTMASK -> {
                        ctx.numHints += args.size / 2
                        val maskSize = (ctx.numHints + 7) / 8
                        for (i in 0 until maskSize) { buffer.uint8() } // Discard mask
                        args.clear()
                    }
                    CNTMASK -> {
                        ctx.numHints += args.size / 2
                        val maskSize = (ctx.numHints + 7) / 8
                        for (i in 0 until maskSize) { buffer.uint8() } // Discard mask
                        args.clear()
                    }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Move Ops
                    RMOVETO -> {
                        ctx.x += args[0]
                        ctx.y += args[1]
                        builder.addContour()
                        builder.addPoint(ctx.x, ctx.y, true)
                        args.clear()
                    }
                    HMOVETO -> {
                        ctx.x += args[0]
                        builder.addContour()
                        builder.addPoint(ctx.x, ctx.y, true)
                        args.clear()
                    }
                    VMOVETO -> {
                        ctx.y += args[0]
                        builder.addContour()
                        builder.addPoint(ctx.x, ctx.y, true)
                        args.clear()
                    }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Line Op
                    RLINETO -> {
                        for (i in 0 until args.size / 2) {
                            ctx.x += args[i * 2]
                            ctx.y += args[i * 2 + 1]
                            builder.addPoint(ctx.x, ctx.y, true)
                        }
                        args.clear()
                    }
                    HLINETO -> {
                        var phase = true
                        for (i in 0 until args.size) {
                            if (phase)
                                ctx.x += args[i]
                            else
                                ctx.y += args[i]
                            builder.addPoint(ctx.x, ctx.y, true)
                            phase = !phase
                        }
                        args.clear()
                    }
                    VLINETO -> {
                        var phase = true
                        for (i in 0 until args.size) {
                            if (phase)
                                ctx.y += args[i]
                            else
                                ctx.x += args[i]
                            builder.addPoint(ctx.x, ctx.y, true)
                            phase = !phase
                        }
                        args.clear()
                    }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Curve Op
                    RRCURVETO -> {
                        for (i in 0 until args.size / 6) {
                            val phaseHead = 6 * i
                            ctx.x += args[phaseHead] // dxa
                            ctx.y += args[phaseHead + 1] // dya
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 2] // dxb
                            ctx.y += args[phaseHead + 3] // dyb
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 4] // dxc
                            ctx.y += args[phaseHead + 5] // dyc
                            builder.addPoint(ctx.x, ctx.y, true)
                        }
                        args.clear()
                    }
                    RCURVELINE -> {
                        val curveCount = (args.size - 2) / 6
                        for (i in 0 until curveCount) {
                            val phaseHead = 6 * i
                            ctx.x += args[phaseHead] // dxa
                            ctx.y += args[phaseHead + 1] // dya
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 2] // dxb
                            ctx.y += args[phaseHead + 3] // dyb
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 4] // dxc
                            ctx.y += args[phaseHead + 5] // dyc
                            builder.addPoint(ctx.x, ctx.y, true)
                        }

                        val lineHead = args.size - 2
                        ctx.x += args[lineHead]
                        ctx.y += args[lineHead + 1]
                        builder.addPoint(ctx.x, ctx.y, true)
                        args.clear()
                    }
                    RLINECURVE -> {
                        val lineCount = (args.size - 6) / 2
                        for (i in 0 until lineCount) {
                            ctx.x += args[i * 2]
                            ctx.y += args[i * 2 + 1]
                            builder.addPoint(ctx.x, ctx.y, true)
                        }

                        val curveHead = args.size - 6
                        ctx.x += args[curveHead] // dxb
                        ctx.y += args[curveHead + 1] // dyb
                        builder.addPoint(ctx.x, ctx.y, false)
                        ctx.x += args[curveHead + 2] // dxc
                        ctx.y += args[curveHead + 3] // dyc
                        builder.addPoint(ctx.x, ctx.y, false)
                        ctx.x += args[curveHead + 4] // dxd
                        ctx.y += args[curveHead + 5] // dyd
                        builder.addPoint(ctx.x, ctx.y, true)
                        args.clear()
                    }
                    HHCURVETO -> {
                        val dy1Exists = args.size % 4 == 1
                        if (dy1Exists) {
                            ctx.y += args[0]
                        }
                        for (i in 0 until args.size / 4) {
                            val phaseHead = 4 * i + if (dy1Exists) 1 else 0
                            ctx.x += args[phaseHead] // dxa
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 1] // dxb
                            ctx.y += args[phaseHead + 2] // dyb
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 3] // dxc
                            builder.addPoint(ctx.x, ctx.y, true)
                        }
                        args.clear()
                    }
                    VVCURVETO -> {
                        val dx1Exists = args.size % 4 == 1
                        if (dx1Exists) {
                            ctx.x += args[0]
                        }
                        for (i in 0 until args.size / 4) {
                            val phaseHead = 4 * i + if (dx1Exists) 1 else 0
                            ctx.y += args[phaseHead] // dya
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.x += args[phaseHead + 1] // dxb
                            ctx.y += args[phaseHead + 2] // dyb
                            builder.addPoint(ctx.x, ctx.y, false)
                            ctx.y += args[phaseHead + 3] // dyc
                            builder.addPoint(ctx.x, ctx.y, true)
                        }
                        args.clear()
                    }
                    HVCURVETO -> {
                        var phase = false
                        for (i in 0 until args.size / 4) {
                            val phaseHead = 4 * i
                            if (phase) {
                                ctx.y += args[phaseHead] // dya
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 1] // dxb
                                ctx.y += args[phaseHead + 2] // dyb
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 3] // dxc
                                if (phaseHead + 4 + 1 == args.size) { // last dxf is optional
                                    ctx.y += args[phaseHead + 4]
                                }
                                builder.addPoint(ctx.x, ctx.y, true)
                            } else {
                                ctx.x += args[phaseHead] // dxd
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 1] // dxe
                                ctx.y += args[phaseHead + 2] // dye
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.y += args[phaseHead + 3] // dyf
                                if (phaseHead + 4 + 1 == args.size) { // last dxf is optional
                                    ctx.x += args[phaseHead + 4]
                                }
                                builder.addPoint(ctx.x, ctx.y, true)
                            }
                            phase = !phase
                        }
                        args.clear()
                    }
                    VHCURVETO -> {
                        var phase = true
                        for (i in 0 until args.size / 4) {
                            val phaseHead = 4 * i
                            if (phase) {
                                ctx.y += args[phaseHead] // dya
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 1] // dxb
                                ctx.y += args[phaseHead + 2] // dyb
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 3] // dxc
                                if (phaseHead + 4 + 1 == args.size) { // last dxf is optional
                                    ctx.y += args[phaseHead + 4]
                                }
                                builder.addPoint(ctx.x, ctx.y, true)
                            } else {
                                ctx.x += args[phaseHead] // dxd
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.x += args[phaseHead + 1] // dxe
                                ctx.y += args[phaseHead + 2] // dye
                                builder.addPoint(ctx.x, ctx.y, false)
                                ctx.y += args[phaseHead + 3] // dyf
                                if (phaseHead + 4 + 1 == args.size) { // last dxf is optional
                                    ctx.x += args[phaseHead + 4]
                                }
                                builder.addPoint(ctx.x, ctx.y, true)
                            }
                            phase = !phase
                        }
                        args.clear()
                    }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Subroutine Op
                    CALLGSUBR -> {
                        val subrIndex = args.last() + ctx.cff.globalBias
                        args.removeAt(args.lastIndex) // pop subr index
                        val pc = buffer.position() // save pc
                        val (start, end) = readIndex(ctx.buffer, ctx.cff.globalSubrOffset, subrIndex)
                        if(!exec(ctx, start, end)) {
                            return false // terminate if reached to endchar
                        }
                        buffer.position(pc) // restore pc

                    }
                    CALLSUBR -> { 
                        val subrIndex = args.last() + ctx.fd.localBias
                        args.removeAt(args.lastIndex) // pop subr index
                        val pc = buffer.position() // save pc
                        val (start, end) = readIndex(ctx.buffer, ctx.fd.subrOffset, subrIndex)
                        if (!exec(ctx, start, end)) {
                            return false // terminate if reached to endchar
                        }
                        buffer.position(pc) // restore pc
                    }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Others
                    SHORTINT -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    RETURN -> { return true }
                    ENDCHAR -> {return false }

                    ////////////////////////////////////////////////////////////////////////////////
                    // Arithmetic Ops
                    AND -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    OR -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    NOT -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    ABS -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    ADD -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    SUB -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    DIV -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    NEG -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    EQ -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    DROP -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    PUT -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    GET -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    IFELSE -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    RANDOM -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    MUL -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    SQRT -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    DUP -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    EXCH -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    INDEX -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    ROLL -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    HFLEX -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    FLEX -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    HFLEX1 -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    FLEX1 -> { TODO("${getOpName(value.intValue)} is not yet supported") }
                    else -> { throw RuntimeException("Unknown Op: ${value.intValue}")}
                }
            }
            OperationType.ERROR -> {
                throw RuntimeException("Failed to parse operations")
            }
        }
    }
    throw RuntimeException("Subroutine or charString finished without return/endchar")
}

fun execCharString(buffer: ByteBuffer, cff: CFFMetadata, fd: FDMetadata, glyphId: Int): List<Contour> {
    val charStringOffset = cff.topDict.charStringOffset + cff.cffOffset
    val (start, end) = readIndex(buffer, charStringOffset, glyphId)

    val builder = ContourBuilder()
    val ctx = VMContext(buffer = buffer, cff = cff, fd = fd, builder = builder)

    exec(ctx = ctx, startPc = start, endPc = end)

    return builder.build()
}
