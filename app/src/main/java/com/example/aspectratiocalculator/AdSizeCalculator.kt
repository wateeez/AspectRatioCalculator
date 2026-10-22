package com.example.aspectratiocalculator

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sqrt

data class AdSize(
    val widthCm: Double,
    val heightCm: Double,
    val columns: Double,
    val columnCentimeters: Double
) {
    val widthInch get() = widthCm / CM_PER_INCH
    val heightInch get() = heightCm / CM_PER_INCH
    val ratio get() = widthCm / heightCm

    companion object {
        const val CM_PER_INCH = 2.54
    }
}

object AdSizeCalculator {
    /**
     * One column centimeter (cc) is one column wide and 1 cm high, so an ad of [columns] columns
     * and height h cm is columns * h cc.
     *
     * Custom ratio (null): the ad spans all [columns]: width = columns * columnWidth, height = cc / columns.
     * Fixed ratio: [columns] is the page width. Ads are sold in whole columns, so this picks the
     * whole column count (1..columns) whose shape is closest to the ratio; height = cc / n.
     * The ratio actually achieved is [AdSize.ratio].
     */
    fun calculate(cc: Double, columnWidthCm: Double, columns: Double, ratio: Pair<Int, Int>?): AdSize {
        require(cc > 0 && columnWidthCm > 0 && columns > 0) { "cc, column width and columns must be positive" }
        if (ratio == null) {
            return AdSize(columns * columnWidthCm, cc / columns, columns, cc)
        }
        val r = ratio.first.toDouble() / ratio.second
        val maxColumns = floor(columns).toInt().coerceAtLeast(1)
        // width / height = n² * columnWidth / cc, so the ideal (fractional) n is sqrt(cc * r / columnWidth).
        val ideal = sqrt(cc * r / columnWidthCm)
        val n = listOf(floor(ideal).toInt(), floor(ideal).toInt() + 1)
            .map { it.coerceIn(1, maxColumns) }
            .minBy { abs(ln(it * it * columnWidthCm / cc / r)) }
        return AdSize(n * columnWidthCm, cc / n, n.toDouble(), cc)
    }
}
