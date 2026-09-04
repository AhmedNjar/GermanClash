package com.example.germanclash.data.local.questionbank

import com.example.germanclash.domain.model.GameFormat

/**
 * level/category null means "no restriction" - the practice pool includes
 * everything on that axis. sessionLength always has a value - it's how many
 * questions a solo session runs before ending on the Results screen.
 */
data class PracticeFilter(
    val level: String? = null,
    val category: String? = null,
    val sessionLength: Int = 10,
    val format: GameFormat = GameFormat.CLASSIC
)

/**
 * Simple mutable holder so ModeSelectScreen (which has no ViewModel) can set
 * the chosen filter right before starting a solo game, and SoloDataSource can
 * read it when picking questions. Written once from the UI thread before
 * navigation, then read for the rest of that session - not built for
 * concurrent writes.
 */
class PracticeFilterState {
    var current: PracticeFilter = PracticeFilter()
}
