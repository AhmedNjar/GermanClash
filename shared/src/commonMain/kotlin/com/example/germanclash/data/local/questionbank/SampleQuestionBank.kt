package com.example.germanclash.data.local.questionbank

import com.example.germanclash.domain.model.AnswerOption
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Question

/**
 * Placeholder content - a handful of DER_DIE_DAS questions cycling in order.
 * Swap this out for a real vocabulary dataset (bundled JSON, a Firestore
 * collection, whatever content pipeline you build) once one exists - nothing
 * else in the app needs to change, since it only ever talks to QuestionBank.
 */
class SampleQuestionBank : QuestionBank {

    private val questions = listOf(
        Question(
            id = "q1", type = GameType.DER_DIE_DAS, prompt = "___ Hund",
            options = listOf(AnswerOption("der", "der"), AnswerOption("die", "die"), AnswerOption("das", "das")),
            correctAnswerId = "der"
        ),
        Question(
            id = "q2", type = GameType.DER_DIE_DAS, prompt = "___ Katze",
            options = listOf(AnswerOption("der", "der"), AnswerOption("die", "die"), AnswerOption("das", "das")),
            correctAnswerId = "die"
        ),
        Question(
            id = "q3", type = GameType.DER_DIE_DAS, prompt = "___ Haus",
            options = listOf(AnswerOption("der", "der"), AnswerOption("die", "die"), AnswerOption("das", "das")),
            correctAnswerId = "das"
        )
    )

    override fun nextQuestion(previousId: String?, filter: PracticeFilter): Question {
        val currentIndex = questions.indexOfFirst { it.id == previousId }
        val nextIndex = (currentIndex + 1) % questions.size
        return questions[nextIndex]
    }

    override fun availableCategories(): List<String> = emptyList()
    override fun availableLevels(): List<String> = emptyList()
}
