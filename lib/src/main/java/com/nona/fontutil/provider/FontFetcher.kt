package com.nona.fontutil.provider

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.util.Log
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.core.Font
import com.nona.fontutil.core.FontFamily
import com.nona.fontutil.core.otparser.FontStyle
import com.nona.fontutil.core.otparser.styleDistance
import com.nona.fontutil.coroutines.FontCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

import androidx.core.provider.FontsContractCompat.Columns as FontColumns

class FontFetcher(
    context: Context,
    authority: String
) {
    data class FontFileInfo(
        val fileUri: Uri,
        val index: Int,
        val varSettings: String?,
        val weight: Int,
        val italic: Boolean,
        val resultCode: Int
    )

    suspend fun fetchSingleFont(
        name: String,
        weight: Int?,
        italic: Boolean?,
        scope: CoroutineScope = FontCoroutineScope.fontScope
    ) = scope.async async@ {
        val list = fetchFontList(buildQuery(name, weight, italic))
            .filter {
                it.resultCode == 0
            }
        if (list.isEmpty()) return@async null
        if (weight == null && italic == null) {
            return@async createFont(list[0])
        }

        val bestFont = list.minBy {
            styleDistance(
                FontStyle(it.weight, it.italic),
                FontStyle(weight ?: it.weight, italic ?: it.italic)
            )
        } ?: return@async null

        return@async createFont(bestFont)
    }.await()

    // We may fetch in background thread. To avoid leaking the given context, store the appContext
    // ref instead.
    private val appContext = context.applicationContext

    private val uri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
        .authority(authority)
        .build()

    private val fileBaseUri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
        .authority(authority)
        .build()

    private fun buildQuery(name: String, weight: Int?, italic: Boolean?): String {
        var res = "name=$name"
        if (weight != null) {
            res += "&amp;weight=$weight"
        }
        if (italic != null) {
            res += "&amp;italic=$italic"
        }
        return res
    }

    private fun createFont(fontInfo: FontFileInfo): Font? {
        return Font.Builder(appContext, fontInfo.fileUri)
            .setStyle(FontStyle(fontInfo.weight, fontInfo.italic))
            .build()
    }

    private fun fetchFontList(
        query: String
    ): List<FontFileInfo> {

        val cursor = appContext.contentResolver.query(
            uri,  // Uri
            arrayOf(  // projection
                FontColumns._ID,
                FontColumns.FILE_ID,
                FontColumns.TTC_INDEX,
                FontColumns.VARIATION_SETTINGS,
                FontColumns.WEIGHT,
                FontColumns.ITALIC,
                FontColumns.RESULT_CODE
            ),
            "query = ?",  // selection
            arrayOf(query),  // selectionArgs
            null,  // sortOrder
            null  // cancellationSignal
        ) ?: return listOf()

        cursor.use {

            if (cursor.count <= 0) return listOf()

            val resultCodeIndex = cursor.getColumnIndex(FontColumns.RESULT_CODE)
            val idIndex = cursor.getColumnIndex(FontColumns._ID)
            val fileIdIndex = cursor.getColumnIndex(FontColumns.FILE_ID)
            val indexIndex = cursor.getColumnIndex(FontColumns.TTC_INDEX)
            val weightIndex = cursor.getColumnIndex(FontColumns.WEIGHT)
            val italicIndex = cursor.getColumnIndex(FontColumns.ITALIC)
            val varSettingsIndex = cursor.getColumnIndex(FontColumns.VARIATION_SETTINGS)

            val result = mutableListOf<FontFileInfo>()

            while (cursor.moveToNext()) {

                // If result code is missing, assuming it was fine.
                val resultCode = if (resultCodeIndex != -1)
                    cursor.getInt(resultCodeIndex)
                else FontColumns.RESULT_CODE_OK

                val index = if (indexIndex != -1) cursor.getInt(indexIndex) else 0

                val fileUri = if (fileIdIndex == -1) {
                    // This is legacy behavior of GMS Core.
                    // TODO: Consider removing.
                    ContentUris.withAppendedId(uri, cursor.getLong(idIndex))
                } else {
                    ContentUris.withAppendedId(fileBaseUri, cursor.getLong(fileIdIndex))
                }

                // If no weight given, assume it is Regular style.
                val weight = if (weightIndex != -1) cursor.getInt(weightIndex) else 400

                val italic = italicIndex != -1 && cursor.getInt(italicIndex) == 1

                val varSettings = if (varSettingsIndex != -1)
                    cursor.getString(varSettingsIndex)
                else
                    null

                result.add(
                    FontFileInfo(
                        fileUri = fileUri,
                        index = index,
                        weight = weight,
                        italic = italic,
                        varSettings = varSettings,
                        resultCode = resultCode
                    )
                )
            }

            return result
        }
    }

}