package com.example.germanclash.data.local.questionbank

import android.content.Context
import com.example.germanclash.domain.model.Question
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Loads real vocabulary content bundled as JSON assets, one file per level
 * (the map key becomes each question's level tag). Every question also keeps
 * its JSON category, so PracticeFilter can narrow the pool to e.g. "A1 only"
 * or "Animals only" - level/category live only here in the data layer, never
 * on the domain Question, since filtering is a data-layer concern.
 */
class AndroidAssetQuestionBank(
    context: Context,
    assetFilesByLevel: Map<String, String>
) : QuestionBank {

    private data class Entry(val level: String, val category: String, val question: Question)

    private val json = Json { ignoreUnknownKeys = true }

    private val entries: List<Entry> = assetFilesByLevel
        .flatMap { (level, fileName) ->
            val text = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            json.decodeFromString<List<VocabularyQuestionDto>>(text).map { dto ->
                Entry(level = level, category = dto.category.orEmpty(), question = dto.toDomainQuestion())
            }
        }
        .shuffled()

    override fun nextQuestion(previousId: String?, filter: PracticeFilter): Question {
        check(entries.isNotEmpty()) { "No questions loaded - check the asset file names/content" }

        val pool = entries
            .filter { entry ->
                (filter.level == null || entry.level == filter.level) &&
                    (filter.category == null || entry.category == filter.category)
            }
            .map { it.question }
            .ifEmpty { entries.map { it.question } } // filter matched nothing - fall back rather than crash

        val currentIndex = pool.indexOfFirst { it.id == previousId }
        val nextIndex = (currentIndex + 1) % pool.size
        return pool[nextIndex]
    }

    override fun availableCategories(): List<String> =
        entries.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()

    override fun availableLevels(): List<String> =
        entries.map { it.level }.distinct().sorted()
}
