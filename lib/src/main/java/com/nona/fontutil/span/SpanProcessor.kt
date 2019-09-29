package com.nona.fontutil.span

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.core.FontItemizer

data class TypefaceSpan(val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = typeface
    }

    override fun updateDrawState(tp: TextPaint?) {
        tp?.typeface = typeface
    }
}

class SpanProcessor private constructor() {
    companion object {
        fun process(charSequence: CharSequence, fontCollection: FontCollection): CharSequence {
            val runs = FontItemizer(fontCollection).itemize(charSequence)

            // TODO: preseve exisiting spans
            // TODO: Style resolution

            val result = SpannableString(charSequence)
            runs.fold(0) { start, run ->

                if (run.family != null) {
                    result.setSpan(
                        TypefaceSpan(run.family.fonts[0].typeface),
                        start,
                        start + run.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }

                start + run.length
            }

            return result
        }
    }

}