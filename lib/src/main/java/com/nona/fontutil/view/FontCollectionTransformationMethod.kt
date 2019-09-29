import android.graphics.Rect
import android.text.method.TransformationMethod
import android.view.View
import com.nona.fontutil.core.FontCollection
import com.nona.fontutil.span.SpanProcessor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking

class FontCollectionTransformationMethod(
    val parent: TransformationMethod?,
    val deferredCollection: Deferred<FontCollection?>
): TransformationMethod {
    override fun onFocusChanged(
        view: View?,
        sourceText: CharSequence?,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        parent?.onFocusChanged(view, sourceText, focused, direction, previouslyFocusedRect)
    }

    override fun getTransformation(source: CharSequence?, view: View?): CharSequence = runBlocking {
        val parentProcessed = parent?.getTransformation(source, view) ?: source ?: ""
        val collection = deferredCollection.await()
        if (collection == null)
            parentProcessed
        else
            SpanProcessor.process(parentProcessed, collection)
    }
}