package com.nona.fontutil.base

fun Int.toUnsigned(): Long = toLong() and 0xFFFF_FFFFL
fun Short.toUnsigned(): Int = toInt() and 0xFFFF
fun Byte.toUnsigned(): Int = toInt() and 0xFF