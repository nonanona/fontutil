package com.nona.fontutil.base

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException

object FileUtil {

    data class AutoUnlinkFile(val file: File): AutoCloseable {
        override fun close() {
            file.delete()
        }
    }

    private fun createTemporaryFile(context: Context, suffix: String): File =
        File.createTempFile("fontutil-", suffix, context.cacheDir)

    fun copyToTemporaryFile(context: Context, @RawRes fontResId: Int): AutoUnlinkFile {
        return AutoUnlinkFile(createTemporaryFile(context, "ttf").apply {
            FileOutputStream(this).use { output ->
                context.resources.openRawResource(fontResId).use { input ->
                    input.copyTo(output)
                }
            }
        })
    }

    fun copyToTemporaryFile(context: Context, uri: Uri): AutoUnlinkFile {
        return AutoUnlinkFile(createTemporaryFile(context, "ttf").apply {
            FileOutputStream(this).use { output ->
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                } ?: throw RuntimeException("Provider didn't give stream for $uri")
            }
        })
    }
}