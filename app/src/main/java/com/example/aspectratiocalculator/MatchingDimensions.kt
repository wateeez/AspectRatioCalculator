package com.example.aspectratiocalculator

/** Whole-pixel sizes that share a given aspect ratio, picked from common screen/image sizes. */
object MatchingDimensions {
    private val COMMON = listOf(
        640, 720, 768, 800, 960, 1024, 1080, 1152, 1280, 1440, 1600,
        1920, 2160, 2560, 3200, 3840, 4096, 4320, 5120, 7680,
    )
    private const val MIN_SIZES = 4

    private val NAMES = mapOf(
        640 to 360 to "nHD", 1280 to 720 to "HD", 1920 to 1080 to "Full HD", 2560 to 1440 to "QHD (2K)",
        3840 to 2160 to "4K UHD", 7680 to 4320 to "8K UHD", 640 to 480 to "VGA", 800 to 600 to "SVGA",
        1024 to 768 to "XGA", 1280 to 1024 to "SXGA", 1600 to 1200 to "UXGA", 1080 to 1080 to "Square HD",
        1080 to 1920 to "Vertical Full HD", 720 to 1280 to "Vertical HD", 4096 to 2160 to "DCI 4K",
    )

    /** Common name for a size such as "Full HD", or null when it has none. */
    fun nameOf(width: Int, height: Int): String? = NAMES[width to height]

    /** [ratioWidth]:[ratioHeight] must already be in lowest terms. Sorted by width. */
    fun forRatio(ratioWidth: Int, ratioHeight: Int): List<Pair<Int, Int>> {
        if (ratioWidth <= 0 || ratioHeight <= 0) return emptyList()
        val sizes = sortedSetOf<Pair<Int, Int>>(compareBy({ it.first }, { it.second }))
        for (v in COMMON) {
            // v as the width, then v as the height; keep only exact whole-pixel results.
            if ((v.toLong() * ratioHeight) % ratioWidth == 0L) sizes += v to (v.toLong() * ratioHeight / ratioWidth).toInt()
            if ((v.toLong() * ratioWidth) % ratioHeight == 0L) sizes += (v.toLong() * ratioWidth / ratioHeight).toInt() to v
        }
        if (sizes.size < MIN_SIZES) {
            // Unusual ratio: fall back to doubling multiples of the ratio.
            var k = (320 + ratioWidth - 1) / ratioWidth
            while (k.toLong() * ratioWidth <= 7680 && sizes.size < 8) {
                sizes += k * ratioWidth to k * ratioHeight
                k *= 2
            }
        }
        return sizes.toList()
    }
}
