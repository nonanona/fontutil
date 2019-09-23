package com.nona.fontutil.systemfont

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemFontsTest {
    @Test
    fun loadSystemFonts() {
        assertThat(SystemFonts.retrieve()).isNotEmpty()
    }
}