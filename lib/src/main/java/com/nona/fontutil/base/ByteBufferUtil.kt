@file:Suppress("NOTHING_TO_INLINE")

package com.nona.fontutil.base

import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

inline fun ByteBuffer.int64(): Long = long
inline fun ByteBuffer.uint32(): Long = int.toUnsigned()
inline fun ByteBuffer.int32(): Int = int
inline fun ByteBuffer.uint16(): Int = short.toUnsigned()
inline fun ByteBuffer.int16(): Int = short.toInt()
inline fun ByteBuffer.uint8(): Int = get().toUnsigned()
inline fun ByteBuffer.int8(): Int = get().toInt()

fun ByteBuffer.uint24(): Int {
    val b0 = uint8()
    val b1 = uint8()
    val b2 = uint8()
    return (b0 shl 16) or (b1 shl 8) or b2
}

inline fun ByteBuffer.uint32(i: Int): Long = getInt(i).toUnsigned()
inline fun ByteBuffer.int32(i: Int): Int = getInt(i)
inline fun ByteBuffer.uint16(i: Int): Int = getShort(i).toUnsigned()
inline fun ByteBuffer.int16(i: Int): Int = getShort(i).toInt()
inline fun ByteBuffer.uint8(i: Int): Int = get(i).toUnsigned()
inline fun ByteBuffer.int8(i: Int): Int = get(i).toInt()

// We are not supporting over 2GB font files. Just cast to Int.
inline fun ByteBuffer.position(i: Long) = position(i.toInt())

inline fun ShortBuffer.uint16(): Int = get().toUnsigned()
inline fun ShortBuffer.uint16(i: Int): Int = get(i).toUnsigned()
inline fun ShortBuffer.intt16(): Int = get().toInt()
inline fun ShortBuffer.intt16(i: Int): Int = get(i).toInt()

inline fun IntBuffer.uint32(): Long = get().toUnsigned()
inline fun IntBuffer.uint32(i: Int): Long = get(i).toUnsigned()
inline fun IntBuffer.intt32(): Int = get()
inline fun IntBuffer.intt32(i: Int): Int = get(i)