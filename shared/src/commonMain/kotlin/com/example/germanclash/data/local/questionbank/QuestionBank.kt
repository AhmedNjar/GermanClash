package com.example.germanclash.data.local.questionbank

import com.example.germanclash.domain.model.Question

interface QuestionBank {
    fun nextQuestion(previousId: String?, filter: PracticeFilter): Question

    /** Real categories/levels present in the loaded content, for a picker that stays in sync with the data. */
    fun availableCategories(): List<String>
    fun availableLevels(): List<String>
}
