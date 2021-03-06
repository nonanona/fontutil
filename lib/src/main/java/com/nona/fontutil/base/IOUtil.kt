package com.nona.fontutil.base

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.FontRes
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

object IOUtil {

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
        return FileInputStream(file).use {
            it.channel.map(FileChannel.MapMode.READ_ONLY, offset, length ?: it.channel.size())
        }?: throw IOException("Failed to mmap $file")
    }

    fun mmap(resolver: ContentResolver, uri: Uri): ByteBuffer {
        return resolver.openFileDescriptor(uri, "r", null)?.use { pfd ->
            FileInputStream(pfd.fileDescriptor).use { fis ->
                val channel = fis.channel
                val size = channel.size()
                channel.map(FileChannel.MapMode.READ_ONLY, 0, size)
            }
        } ?: throw IOException("Failed to mmap $uri")
    }
}