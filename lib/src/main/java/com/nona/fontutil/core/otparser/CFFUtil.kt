package com.nona.fontutil.core.otparser

import com.nona.fontutil.base.*
import java.lang.RuntimeException
import java.nio.ByteBuffer


internal enum class ValueType {
    KEY,
    INTEGER,
    REAL_NUMBER,
    ERROR
}

internal data class DictValue(
    var type: ValueType = ValueType.ERROR,
    var intValue: Int = 0,
    var realValue: String = ""
) {
    override fun toString(): String {
        return when (type) {
            ValueType.KEY -> "key:$intValue"
            ValueType.INTEGER -> "int:$intValue"
            ValueType.REAL_NUMBER -> "real:$realValue"
            ValueType.ERROR -> "error"
        }
    }
}

internal enum class OperationType {
    INTEGER,
    OPERATOR,
    ERROR
}

internal data class Operation(
    var type: OperationType = OperationType.ERROR,
    var intValue: Int = 0
) {
    override fun toString(): String {
        return when (type) {
            OperationType.OPERATOR -> "op:$intValue"
            OperationType.INTEGER -> "int:$intValue"
            OperationType.ERROR -> "error"
        }
    }
}

internal class DictValuePool {
    val list = mutableListOf<DictValue>()
    fun get(): DictValue = if (list.isEmpty()) {
        DictValue()
    } else {
        list.removeAt(list.lastIndex)
    }

    fun recycle(value: DictValue) {
        list.add(value)
    }
}

internal fun readDictValue(buffer: ByteBuffer, out: DictValue) {
    val b0 = buffer.uint8()
    if (b0 in 0..21) {
        out.type = ValueType.KEY
        if (b0 == 12) {
            val b1 = buffer.uint8()
            out.intValue = b0 * 100 + b1
        } else {
            out.intValue = b0
        }
    } else if (b0 in 32..246) {
        out.type = ValueType.INTEGER
        out.intValue = b0 - 139
    } else if (b0 in 247..250) {
        out.type = ValueType.INTEGER
        out.intValue = (b0 - 247) * 256 + buffer.uint8() + 108
    } else if (b0 in 251..254) {
        out.type = ValueType.INTEGER
        out.intValue = -(b0 - 251) * 256 - buffer.uint8() - 108
    } else if (b0 == 28) {
        val b1 = buffer.uint8()
        val b2 = buffer.uint8()
        out.type = ValueType.INTEGER
        out.intValue = (b1 shl 8) or b2
    } else if (b0 == 29) {
        val b1 = buffer.uint8()
        val b2 = buffer.uint8()
        val b3 = buffer.uint8()
        val b4 = buffer.uint8()
        out.type = ValueType.INTEGER
        out.intValue = (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4
    } else if (b0 == 30) {
        var str = ""
        while (true) {
            val v = buffer.uint8()
            val nibble1 = v shr 4
            if (nibble1 == 0x0f) break
            str += NIBBLE_STR[nibble1]
            if (nibble1 == 0x0d) str += "-"  // Nibble d is "E-" NIBBLE_STR only contains "E"

            val nibble2 = v and 0x0F
            if (nibble2 == 0x0f) break
            str += NIBBLE_STR[nibble2]
            if (nibble2 == 0x0d) str += "-"  // Nibble d is "E-" NIBBLE_STR only contains "E"
        }
        out.type = ValueType.REAL_NUMBER
        out.realValue = str
    } else {
        throw RuntimeException("Unsupported Op: $b0")
    }
}

internal fun parseCharString(buffer: ByteBuffer, out: Operation) {
    val b0 = buffer.uint8()
    if (b0 == 12) {
        val b1 = buffer.uint8()
        out.type = OperationType.OPERATOR
        out.intValue = b0 * 100 + b1
    } else if (b0 == 28){
        val b1 = buffer.uint8()
        val b2 = buffer.uint8()
        out.type = OperationType.INTEGER
        out.intValue = (b1 shl 8) or b2
    } else if (0 <= b0 && b0 <= 31) {
        out.type = OperationType.OPERATOR
        out.intValue = b0
    } else if (b0 in 32..246) {
        out.type = OperationType.INTEGER
        out.intValue = b0 - 139
    } else if (b0 in 247..250) {
        out.type = OperationType.INTEGER
        out.intValue = (b0 - 247) * 256 + buffer.uint8() + 108
    } else if (b0 in 251..254) {
        out.type = OperationType.INTEGER
        out.intValue = -(b0 - 251) * 256 - buffer.uint8() - 108
    } else { // b0 == 255
        val b1 = buffer.uint8()
        val b2 = buffer.uint8()
        val b3 = buffer.uint8()
        val b4 = buffer.uint8()
        out.type = OperationType.INTEGER
        out.intValue = (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4
    }
}

internal fun ByteBuffer.offset(size: Int): Long = when (size) {
    1 -> uint8().toLong()
    2 -> uint16().toLong()
    3 -> uint24().toLong()
    4 -> uint32().toLong()
    else -> throw RuntimeException("Invalid offset size: $size")
}

fun readIndex(buffer: ByteBuffer, offset: Int, index: Int): Pair<Long, Long> =
    readIndex(buffer, offset.toLong(), index)

fun readIndex(buffer: ByteBuffer, offset: Long, index: Int): Pair<Long, Long> {
    buffer.position(offset)
    val count = buffer.uint16()
    val offSize = buffer.uint8()
    buffer.position(offset + 3 /* header */ + index * offSize)
    val start = buffer.offset(offSize)
    val end = buffer.offset(offSize)

    val dataStart = offset + 3 /* header */ + (count +  1) * offSize
    return Pair(dataStart + start -1 /* inclusive */, dataStart + end /* exclusive */)
}