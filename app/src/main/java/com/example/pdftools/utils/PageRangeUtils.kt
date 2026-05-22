package com.example.pdftools.utils

object PageRangeUtils {
    /**
     * Parses a page range string (e.g., "1-3, 5") into a sorted, unique list of 0-based page indices.
     */
    fun parsePageRanges(rangeStr: String, totalPages: Int): List<Int> {
        val selectedPages = mutableSetOf<Int>()
        
        // Split by comma
        val parts = rangeStr.split(",")
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue
            
            if (trimmed.contains("-")) {
                val rangeParts = trimmed.split("-")
                if (rangeParts.size == 2) {
                    val startStr = rangeParts[0].trim()
                    val endStr = rangeParts[1].trim()
                    val start = startStr.toIntOrNull()
                    val end = endStr.toIntOrNull()
                    if (start != null && end != null) {
                        val actualStart = minOf(start, end)
                        val actualEnd = maxOf(start, end)
                        for (p in actualStart..actualEnd) {
                            val pageIdx = p - 1
                            if (pageIdx in 0 until totalPages) {
                                selectedPages.add(pageIdx)
                            }
                        }
                    }
                }
            } else {
                val page = trimmed.toIntOrNull()
                if (page != null) {
                    val pageIdx = page - 1
                    if (pageIdx in 0 until totalPages) {
                        selectedPages.add(pageIdx)
                    }
                }
            }
        }
        
        return selectedPages.sorted()
    }
}
