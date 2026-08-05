package com.app.germanclash.data.local.questionbank

import com.app.germanclash.domain.model.Question

interface QuestionBank {
    fun nextQuestion(previousId: String?, filter: PracticeFilter): Question

    /** Real categories/levels present in the loaded content, for a picker that stays in sync with the data. */
    fun availableCategories(): List<String>
    fun availableLevels(): List<String>
}
