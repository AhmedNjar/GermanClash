package com.example.germanclash.data.local.questionbank

import com.example.germanclash.data.local.questionbank.VocabularyQuestionDto
import com.example.germanclash.domain.model.AnswerOption
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Question

/**
 * category/translation from the JSON aren't used yet - Question has no home
 * for them today. Worth adding if you want category filtering (e.g. an
 * A1-only or "Animals only" practice mode) or an Arabic hint on a wrong answer.
 */
fun VocabularyQuestionDto.toDomainQuestion(): Question = Question(
    id = id,
    type = GameType.valueOf(type),
    prompt = prompt,
    imageUrl = imageUrl,
    options = options.map { AnswerOption(id = it, text = it) },
    correctAnswerId = correctAnswer
)
