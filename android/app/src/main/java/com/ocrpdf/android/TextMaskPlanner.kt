package com.ocrpdf.android

import android.graphics.Rect

/**
 * Creates candidate OCR masks only. Do not apply these directly to a PDF:
 * tables, chart axes, equations and artwork need protected-region analysis.
 */
object TextMaskPlanner {
    fun expandedCandidates(
        regions: List<TextRegion>,
        imageWidth: Int,
        imageHeight: Int,
        paddingPx: Int
    ): List<Rect> {
        require(imageWidth > 0 && imageHeight > 0)
        require(paddingPx >= 0)
        return regions.mapNotNull { region ->
            Rect(region.bounds).apply {
                inset(-paddingPx, -paddingPx)
                left = left.coerceIn(0, imageWidth)
                top = top.coerceIn(0, imageHeight)
                right = right.coerceIn(0, imageWidth)
                bottom = bottom.coerceIn(0, imageHeight)
            }.takeIf { !it.isEmpty }
        }
    }
}
