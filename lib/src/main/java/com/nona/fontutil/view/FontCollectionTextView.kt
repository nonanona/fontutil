package com.nona.fontutil.view

import FontCollectionTransformationMethod
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.nona.fontutil.R
import com.nona.fontutil.assets.AssetsXMLParser

open class FontCollectionTextView : TextView {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {

        val a = context.theme.obtainStyledAttributes(attrs,
            R.styleable.FontCollectionTextView, 0, 0)

        val xml = a.getString(R.styleable.FontCollectionTextView_fontCollection)

        val collection = AssetsXMLParser.parseFontCollectionXmlAsync(context.applicationContext, xml)

        transformationMethod = FontCollectionTransformationMethod(
            transformationMethod, collection)
    }
}