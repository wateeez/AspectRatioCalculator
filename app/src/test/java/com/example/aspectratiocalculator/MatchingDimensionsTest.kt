package com.example.aspectratiocalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingDimensionsTest {
    @Test
    fun sixteenByNineIncludesStandardResolutions() {
        val sizes = MatchingDimensions.forRatio(16, 9)
        listOf(1280 to 720, 1920 to 1080, 2560 to 1440, 3840 to 2160).forEach { assertTrue(it in sizes) }
    }

    @Test
    fun everySizeKeepsTheRatioExactly() {
        listOf(16 to 9, 4 to 3, 21 to 9, 1 to 1, 9 to 16, 7 to 5).forEach { (a, b) ->
            MatchingDimensions.forRatio(a, b).forEach { (w, h) -> assertEquals(w.toLong() * b, h.toLong() * a) }
        }
    }

    @Test
    fun unusualRatioStillGetsSizes() {
        assertTrue(MatchingDimensions.forRatio(7, 5).size >= 4)
    }

    @Test
    fun invalidRatioIsEmpty() {
        assertTrue(MatchingDimensions.forRatio(0, 9).isEmpty())
    }
}
