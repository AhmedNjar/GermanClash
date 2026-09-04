package com.example.germanclash.domain.policy

/**
 * A simple, single currency (total correct answers ever) drives all
 * unlocks - easy to understand, and reuses a stat already being tracked
 * for the Stats screen. Starter categories are open from the start;
 * everything else unlocks one at a time as you rack up correct answers.
 */
object UnlockPolicy {
    private val STARTER_CATEGORIES = setOf("Food", "Animals", "Body")
    private const val CORRECT_ANSWERS_PER_CATEGORY_UNLOCK = 20
    private const val CORRECT_ANSWERS_FOR_A2 = 50

    fun isCategoryUnlocked(category: String, allCategoriesSorted: List<String>, totalCorrectAnswers: Int): Boolean {
        if (category in STARTER_CATEGORIES) return true
        val requirement = correctAnswersNeededForCategory(category, allCategoriesSorted) ?: return true
        return totalCorrectAnswers >= requirement
    }

    /** Null means already unlocked (starter category, or not found in the list). */
    fun correctAnswersNeededForCategory(category: String, allCategoriesSorted: List<String>): Int? {
        if (category in STARTER_CATEGORIES) return null
        val lockable = allCategoriesSorted.filterNot { it in STARTER_CATEGORIES }
        val index = lockable.indexOf(category)
        if (index == -1) return null
        return (index + 1) * CORRECT_ANSWERS_PER_CATEGORY_UNLOCK
    }

    fun isLevelUnlocked(level: String, totalCorrectAnswers: Int): Boolean =
        level == "A1" || totalCorrectAnswers >= CORRECT_ANSWERS_FOR_A2

    /** Null means already unlocked. */
    fun correctAnswersNeededForLevel(level: String): Int? =
        if (level == "A1") null else CORRECT_ANSWERS_FOR_A2
}