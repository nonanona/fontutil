package com.nona.fontutil.demo.views

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.nona.fontutil.demo.R
import com.nona.fontutil.demo.databinding.CustomBindingActivityBinding
import com.nona.fontutil.view.bindings.FontCollectionBindingAdapterHelper

@BindingAdapter("app:fontCollection")
fun View.setFontCollection(xmlPath: String) {
    if (this !is TextView) return
    FontCollectionBindingAdapterHelper.updateTextView(this, xmlPath)
}

class CustomBindingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<CustomBindingActivityBinding>(
            this, R.layout.custom_binding_activity)
    }
}