package com.nona.fontutil.assets

import java.lang.RuntimeException
import java.util.*

class CustomTagParserManager private constructor(){
    companion object {
        private val lock = Object()

        private val tagMap = mutableMapOf<String, CustomTagParser>()

        fun register(tag: String, parser: CustomTagParser) {
            synchronized(lock) {
                if (tag in tagMap) {
                    throw RuntimeException("$tag is already registered")
                }
                tagMap.put(tag, parser)
            }
        }

        fun obtainParser(tag: String): CustomTagParser? {
            synchronized(lock) {
                return tagMap[tag]
            }
        }
    }

}