package com.nona.fontutil.test

import java.io.File

private const val THIRD_PARTY_DIR = "../third_party/"

class TestUtil {
    companion object {
        fun getThirdPartyFile(relPath: String) = File(THIRD_PARTY_DIR + relPath)
    }
}