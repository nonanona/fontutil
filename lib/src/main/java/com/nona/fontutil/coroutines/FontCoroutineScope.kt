package com.nona.fontutil.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class FontCoroutineScope : CoroutineScope {

    companion object {
        val fontScope = FontCoroutineScope()
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}