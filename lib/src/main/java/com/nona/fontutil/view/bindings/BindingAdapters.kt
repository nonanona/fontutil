package com.nona.fontutil.view.bindings

import FontCollectionTransformationMethod
import android.widget.TextView
import com.nona.fontutil.assets.AssetsXMLParser
import com.nona.fontutil.coroutines.FontCoroutineScope
import kotlinx.coroutines.CoroutineScope

object FontCollectionBindingAdapterHelper {

    fun updateTextView(
        textView: TextView,
        xmlPath: String,
        scope: CoroutineScope = FontCoroutineScope.fontScope
    ) {
        val collection = AssetsXMLParser.parseFontCollectionXmlAsync(
            textView.context.applicationContext, xmlPath, scope)
        textView.transformationMethod = FontCollectionTransformationMethod(
            textView.transformationMethod, collection)
    }
}