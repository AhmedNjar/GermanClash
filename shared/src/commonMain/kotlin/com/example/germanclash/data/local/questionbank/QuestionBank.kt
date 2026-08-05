package com.example.germanclash.data.local.questionbank

import com.example.germanclash.domain.model.Question

interface QuestionBank {
    fun nextQuestion(previousId: String?): Question
}