package com.example.germanclash.presentation.common

import androidx.compose.ui.graphics.Color

private val CategoryPalette = listOf(
    Color(0xFFFF6B6B), // coral
    Color(0xFF4ECDC4), // teal
    Color(0xFFFFD166), // amber
    Color(0xFF9B5DE5), // violet
    Color(0xFF45B7D1), // sky blue
    Color(0xFFEF476F), // rose
    Color(0xFF06D6A0), // mint
    Color(0xFFF77F00)  // orange
)

private val NeutralAccent = Color(0xFF8A8FA3)

/**
 * Deterministic by name (not index) so a category keeps the same color
 * across launches even as the underlying category list grows.
 */
fun colorForCategory(category: String?): Color {
    if (category == null) return NeutralAccent
    val index = (category.hashCode() and Int.MAX_VALUE) % CategoryPalette.size
    return CategoryPalette[index]
}

private val CategoryEmoji = mapOf(
    "Animals" to "\uD83D\uDC3E",
    "Food" to "\uD83C\uDF4E",
    "Fruits" to "\uD83C\uDF47",
    "Drinks" to "\u2615",
    "Body" to "\uD83D\uDC64",
    "Home" to "\uD83C\uDFE0",
    "City" to "\uD83C\uDFD9",
    "Time" to "\u23F0",
    "Nature" to "\uD83C\uDF3F",
    "Clothing" to "\uD83D\uDC55",
    "School" to "\uD83C\uDF93",
    "Work" to "\uD83D\uDCBC",
    "Transport" to "\uD83D\uDE8C",
    "Objects" to "\uD83D\uDCE6",
    "Family" to "\uD83D\uDC6A",
    "Kitchen" to "\uD83C\uDF73",
    "Bathroom" to "\uD83E\uDEA5",
    "Tools" to "\uD83E\uDDF0",
    "Sports" to "\u26BD",
    "Weather" to "\uD83C\uDF24"
)

/** Falls back to a generic book for any category not in the curated map above. */
fun emojiForCategory(category: String?): String {
    if (category == null) return "\uD83C\uDF10" // globe - "all categories"
    return CategoryEmoji[category] ?: "\uD83D\uDCDA"
}
