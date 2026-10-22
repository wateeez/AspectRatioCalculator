package com.example.aspectratiocalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AdSizeCalculatorTest {
    @Test
    fun customRatioSpansAllColumns() {
        val s = AdSizeCalculator.calculate(cc = 30.0, columnWidthCm = 4.0, columns = 3.0, ratio = null)
        assertEquals(12.0, s.widthCm, 1e-9)
        assertEquals(10.0, s.heightCm, 1e-9)
        assertEquals(12.0 / 2.54, s.widthInch, 1e-9)
    }

    @Test
    fun fixedRatioHitsExactShapeWhenWholeColumnsAllowIt() {
        val s = AdSizeCalculator.calculate(cc = 32.0, columnWidthCm = 4.0, columns = 8.0, ratio = 2 to 1)
        assertEquals(4.0, s.columns, 1e-9)
        assertEquals(16.0, s.widthCm, 1e-9)
        assertEquals(8.0, s.heightCm, 1e-9)
        assertEquals(2.0, s.ratio, 1e-9)
    }

    @Test
    fun fixedRatioUsesWholeColumnsClosestToRatio() {
        // Ideal is 3.65 columns; 4 columns (18 x 15 cm, ratio 1.2) is closer to 1:1 than 3 (13.5 x 20).
        val s = AdSizeCalculator.calculate(cc = 60.0, columnWidthCm = 4.5, columns = 8.0, ratio = 1 to 1)
        assertEquals(4.0, s.columns, 1e-9)
        assertEquals(18.0, s.widthCm, 1e-9)
        assertEquals(15.0, s.heightCm, 1e-9)
        assertEquals(60.0, s.columns * s.heightCm, 1e-9)
    }

    @Test
    fun fixedRatioIsCappedAtThePageWidth() {
        val s = AdSizeCalculator.calculate(cc = 200.0, columnWidthCm = 4.5, columns = 8.0, ratio = 2 to 1)
        assertEquals(8.0, s.columns, 1e-9)
        assertEquals(36.0, s.widthCm, 1e-9)
        assertEquals(25.0, s.heightCm, 1e-9)
    }

    @Test
    fun rejectsInvalidInput() {
        assertThrows(IllegalArgumentException::class.java) {
            AdSizeCalculator.calculate(0.0, 4.0, 3.0, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AdSizeCalculator.calculate(30.0, 4.0, 0.0, 1 to 1)
        }
    }
}
