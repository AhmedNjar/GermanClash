package com.example.germanclash.data.local.questionbank

import android.content.Context
import com.example.germanclash.domain.model.Question
import kotlinx.serialization.json.Json

/**
 * Loads real vocabulary content bundled as JSON assets (vocabulary_a1.json,
 * vocabulary_a2.json) instead of SampleQuestionBank's 3 placeholder questions.
 * Every file's questions are pooled together and shuffled once at load, then
 * cycled through in that fixed shuffled order.
 */
class AndroidAssetQuestionBank(
    private val context: Context,
    private val assetFileNames: List<String>
) : QuestionBank {

    private val json = Json { ignoreUnknownKeys = true }

    private val questions: List<Question> = assetFileNames
        .flatMap { fileName ->
            val text = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            json.decodeFromString<List<VocabularyQuestionDto>>(text)
        }
        .map { it.toDomainQuestion() }
        .shuffled()

    override fun nextQuestion(previousId: String?): Question {
        check(questions.isNotEmpty()) { "No questions loaded - check the asset file names/content" }
        val currentIndex = questions.indexOfFirst { it.id == previousId }
        val nextIndex = (currentIndex + 1) % questions.size
        return questions[nextIndex]
    }
}
