package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.position
import com.nona.fontutil.base.uint8
import java.nio.ByteBuffer


private val OPERATOR_NAMES = arrayOf(
    "-Reserved-", "hstem", "-Reserved-", "vstem", "vmoveto", "rlineto", "hlineto", "vlineto",
    "rrcurveto", "-Reserved-", "callsubr", "return", "escape", "-Reserved-", "endchar", "-Reserved-",
    "-Reserved-", "-Reserved-", "hstemhm", "hintmask", "cntrmask", "rmoveto", "hmoveto", "vstemhm",
    "rcurveline", "rlinecurve", "vvcurveto", "hhcurveto", "shortint", "callgsubr", "vhcurveto", "hvcurveto"
)

private val OPERATOR_EXTENDED_NAMES = arrayOf(
    "-Reserved-", "-Reserved-", "-Reserved-", "and", "or", "not", "-Reserved-", "-Reserved-",
    "-Reserved-", "abs", "add", "sub", "div", "-Reserved-", "neg", "eq",
    "-Reserved-", "-Reserved-", "drop", "-Reserved-", "put", "get", "ifelse", "random",
    "mul", "-Reserved-", "sqrt", "dup", "exch", "index", "roll", "-Reserved-",
    "-Reserved-", "-Reserved-", "hflex", "flex", "hflex1", "flex1", "-Reserved-"
)

internal fun getOpName(op: Int) =
    if (op >= 1200) {
        val ex = op / 100
        if (ex > 38) "NOT_A_OP" else OPERATOR_EXTENDED_NAMES[ex]
    } else if (op <= 31) {
        OPERATOR_NAMES[op]
    } else {
        "NOT_A_OP"
    }