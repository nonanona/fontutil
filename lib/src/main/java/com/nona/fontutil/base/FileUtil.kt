package com.nona.fontutil.base

import android.content.Context
import androidx.annotation.RawRes
import java.io.File
import java.io.FileOutputStream

class FileUtil private constructor() {
    companion object {

        data class AutoUnlinkFile(val file: File): AutoCloseable {
            override fun close() {
                file.delete()
            }
        }

        fun createTemporaryFile(context: Context, suffix: String) =
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
    }
}