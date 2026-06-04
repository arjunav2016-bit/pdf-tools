package com.example.pdftools.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coverage for the pure-logic helpers in [PageRangeUtils]. These tests run under
 * the standard JUnit 4 local unit-test runner (no Robolectric required because
 * the class has no Android dependencies).
 */
class PageRangeUtilsTest {

    // region parsePageRanges

    @Test
    fun parsePageRanges_singlePage() {
        assertEquals(listOf(0, 2, 4), PageRangeUtils.parsePageRanges("1, 3, 5", totalPages = 10))
    }

    @Test
    fun parsePageRanges_simpleRange() {
        assertEquals(listOf(0, 1, 2, 3, 4), PageRangeUtils.parsePageRanges("1-5", totalPages = 10))
    }

    @Test
    fun parsePageRanges_mixedRangesAndSingles() {
        assertEquals(
            listOf(0, 1, 2, 4, 6, 7, 8),
            PageRangeUtils.parsePageRanges("1-3, 5, 7-9", totalPages = 10),
        )
    }

    @Test
    fun parsePageRanges_reversedRangeSortsAndExpands() {
        // "5-1" should be treated as 1..5, not rejected.
        assertEquals(listOf(0, 1, 2, 3, 4), PageRangeUtils.parsePageRanges("5-1", totalPages = 10))
    }

    @Test
    fun parsePageRanges_duplicatesCollapse() {
        // 2, 2-4 should produce 1,2,3 once each.
        assertEquals(listOf(1, 2, 3), PageRangeUtils.parsePageRanges("2, 2-4", totalPages = 10))
    }

    @Test
    fun parsePageRanges_outOfBoundsAreDropped() {
        // Page 99 and -5 don't exist; only 3 and 4 survive.
        assertEquals(listOf(2, 3), PageRangeUtils.parsePageRanges("3-4, 99, -5", totalPages = 5))
    }

    @Test
    fun parsePageRanges_emptyStringReturnsEmpty() {
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("", totalPages = 10))
    }

    @Test
    fun parsePageRanges_whitespaceOnlyReturnsEmpty() {
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("   ", totalPages = 10))
    }

    @Test
    fun parsePageRanges_emptyPartsAreSkipped() {
        // Trailing/leading commas and consecutive commas should be tolerated.
        assertEquals(listOf(0, 1, 2), PageRangeUtils.parsePageRanges(", 1-3 ,", totalPages = 5))
    }

    @Test
    fun parsePageRanges_nonNumericPartIsIgnored() {
        // "abc" silently ignored; "2-3" still parses.
        assertEquals(listOf(1, 2), PageRangeUtils.parsePageRanges("abc, 2-3", totalPages = 10))
    }

    @Test
    fun parsePageRanges_rangeWithMoreThanTwoDashesIsIgnored() {
        // "1-2-3" splits into 3 parts, the range branch returns without adding pages.
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("1-2-3", totalPages = 10))
    }

    @Test
    fun parsePageRanges_zeroIsOutOfBoundsForOneBased() {
        // "0" → pageIdx = -1, fails the inRange check.
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("0", totalPages = 5))
    }

    @Test
    fun parsePageRanges_upperBoundInclusive() {
        assertEquals(listOf(4), PageRangeUtils.parsePageRanges("5", totalPages = 5))
    }

    @Test
    fun parsePageRanges_zeroTotalPagesReturnsEmpty() {
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("1-3", totalPages = 0))
    }

    @Test
    fun parsePageRanges_negativeTotalPagesReturnsEmpty() {
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("1-3", totalPages = -5))
    }

    @Test
    fun parsePageRanges_outputIsSorted() {
        // Provide pages in reverse order; verify sorted ascending output.
        assertEquals(listOf(0, 1, 2, 3, 4), PageRangeUtils.parsePageRanges("5, 3, 1, 2, 4", totalPages = 10))
    }

    @Test
    fun parsePageRanges_singlePageStringWithDashButNoSecondPart() {
        // "1-" has only one part after split, so the else-branch (single page) tries
        // to parse "1-" as an Int and fails — no pages added.
        assertEquals(emptyList<Int>(), PageRangeUtils.parsePageRanges("1-", totalPages = 10))
    }

    // endregion

    // region formatPageRanges

    @Test
    fun formatPageRanges_emptyCollectionReturnsEmptyString() {
        assertEquals("", PageRangeUtils.formatPageRanges(emptyList()))
    }

    @Test
    fun formatPageRanges_collectionWithOnlyNegativesReturnsEmpty() {
        // All filtered out, sorted list is empty → empty string.
        assertEquals("", PageRangeUtils.formatPageRanges(listOf(-1, -2, -3)))
    }

    @Test
    fun formatPageRanges_singlePage() {
        assertEquals("1", PageRangeUtils.formatPageRanges(listOf(0)))
    }

    @Test
    fun formatPageRanges_consecutivePagesFormRange() {
        assertEquals("1-3", PageRangeUtils.formatPageRanges(listOf(0, 1, 2)))
    }

    @Test
    fun formatPageRanges_disjointPagesAreCommaSeparated() {
        assertEquals("1, 3, 5", PageRangeUtils.formatPageRanges(listOf(0, 2, 4)))
    }

    @Test
    fun formatPageRanges_mixedSinglesAndRanges() {
        assertEquals("1-2, 4, 6-7", PageRangeUtils.formatPageRanges(listOf(0, 1, 3, 5, 6)))
    }

    @Test
    fun formatPageRanges_unsortedInputIsSorted() {
        assertEquals("1-3", PageRangeUtils.formatPageRanges(listOf(2, 0, 1)))
    }

    @Test
    fun formatPageRanges_duplicatesAreCollapsed() {
        assertEquals("1-2", PageRangeUtils.formatPageRanges(listOf(0, 0, 1, 1, 1)))
    }

    @Test
    fun formatPageRanges_negativesAreFilteredOut() {
        // -1 filtered, 0 and 1 remain.
        assertEquals("1-2", PageRangeUtils.formatPageRanges(listOf(-1, 0, 1)))
    }

    @Test
    fun formatPageRanges_consecutiveAcrossRangeBoundaryAppends() {
        // 0,1,2,3,5 → range 1-4 then single 6
        assertEquals("1-4, 6", PageRangeUtils.formatPageRanges(listOf(0, 1, 2, 3, 5)))
    }

    @Test
    fun formatPageRanges_largeContiguousBlock() {
        val pages = (0..9).toList()
        assertEquals("1-10", PageRangeUtils.formatPageRanges(pages))
    }

    @Test
    fun formatPageRanges_singleNegativeThenValid() {
        // The negative is dropped, leaving a single page.
        assertEquals("5", PageRangeUtils.formatPageRanges(listOf(-1, 4)))
    }

    // endregion

    // region round-trip

    @Test
    fun roundTrip_parseThenFormatIsStable() {
        val input = "1-3, 5, 7-9"
        val parsed = PageRangeUtils.parsePageRanges(input, totalPages = 10)
        val formatted = PageRangeUtils.formatPageRanges(parsed)
        // Should match a normalized representation.
        assertEquals("1-3, 5, 7-9", formatted)
    }

    @Test
    fun roundTrip_formatThenParseMatchesInput() {
        val pages = listOf(0, 1, 4, 5, 6, 9)
        val formatted = PageRangeUtils.formatPageRanges(pages) // "1-2, 5-7, 10"
        val reparsed = PageRangeUtils.parsePageRanges(formatted, totalPages = 12)
        assertEquals(pages, reparsed)
    }

    @Test
    fun parsePageRanges_doesNotMutateInput() {
        // parsePageRanges should not throw on weird inputs; sanity check.
        val input = "1, 2, 3"
        val out = PageRangeUtils.parsePageRanges(input, totalPages = 5)
        assertEquals(listOf(0, 1, 2), out)
        assertEquals("1, 2, 3", input) // unchanged
    }

    @Test
    fun formatPageRanges_emptyAndSingleEdgeHandled() {
        assertTrue(PageRangeUtils.formatPageRanges(emptyList()).isEmpty())
        assertEquals("1", PageRangeUtils.formatPageRanges(listOf(0)))
    }
}
