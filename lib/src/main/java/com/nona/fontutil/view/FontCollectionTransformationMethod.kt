import android.graphics.Rect
import android.text.method.TransformationMethod
import android.view.View
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.span.SpanProcessor

class FontCollectionTransformationMethod(
    val parent: TransformationMethod?,
    val collection: FontCollection
) : TransformationMethod {
    override fun onFocusChanged(
        view: View?,
        sourceText: CharSequence?,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        parent?.onFocusChanged(view, sourceText, focused, direction, previouslyFocusedRect)
    }

    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        val parentProcessed = parent?.getTransformation(source, view) ?: source ?: return ""

        return SpanProcessor.process(parentProcessed, collection)
    }

}