package com.ocrpdf.android

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class TextMaskPlannerTest {
    @Test
    fun expandsAndClampsRegionToImageBounds() {
        val result = TextMaskPlanner.expandedCandidates(
            listOf(TextRegion("hello", Rect(2, 3, 20, 25))),
            imageWidth = 100,
            imageHeight = 100,
            paddingPx = 5
        )
        assertEquals(1, result.size)
        assertEquals(Rect(0, 0, 25, 30), result.single())
    }

    @Test
    fun dropsRegionOutsideImageAfterClamping() {
        val result = TextMaskPlanner.expandedCandidates(
            listOf(TextRegion("outside", Rect(120, 120, 140, 140))),
            imageWidth = 100,
            imageHeight = 100,
            paddingPx = 0
        )
        assertEquals(0, result.size)
    }
}
