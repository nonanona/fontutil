package com.nona.fontutil.core.otparser

import java.io.IOException

fun checkFormat(check: Boolean, f: () -> String) {
    if (!check) {
        throw IOException(f())
    }
}