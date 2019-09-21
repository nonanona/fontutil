package com.nona.fontutil.core.util

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.annotation.FontRes
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class IOUtil private constructor() {
    companion object {

        // Returns the ByteBuffer of the font file in asset dir.
        fun mmap(asset: AssetManager, filePath: String): ByteBuffer {
            val fd = asset.openFd(filePath)
            return fd.createInputStream()?.use {
                it.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.length)
            } ?: throw IOException("Failed to mmap $filePath.")
        }

        @SuppressLint("ResourceType")
        fun mmap(resources: Resources, @FontRes fontResId: Int): ByteBuffer {
            val fd = resources.openRawResourceFd(fontResId)
            return fd.createInputStream()?.use {
                it.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.length)
            } ?: throw IOException("Failed to mmap $fontResId")
        }

        fun mmap(file: File, offset: Long = 0, length: Long? = null): ByteBuffer {
            val realLength = length ?: file.length()
            return FileInputStream(file).use {
                it.channel.map(FileChannel.MapMode.READ_ONLY, offset, realLength)
            }?: throw IOException("Failed to mmap $file")
        }
    }
}