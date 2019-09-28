package com.nona.fontutil.view.bindings

import FontCollectionTransformationMethod
import android.widget.TextView
import com.nona.fontutil.assets.AssetsXMLParser

object FontCollectionBindingAdapterHelper {

    fun updateTextView(textView: TextView, xmlPath: String) {
        val collection = AssetsXMLParser.parseFontCollectionXml(textView.context, xmlPath) ?: return
        textView.transformationMethod = FontCollectionTransformationMethod(
            textView.transformationMethod, collection)
    }
}