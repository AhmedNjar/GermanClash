package com.example.germanclash.data.local.questionbank

import com.example.germanclash.domain.model.AnswerOption
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Question

/**
 * category is used for filtering (PracticeFilter) but stays out of the
 * domain Question - it's a data-layer-only concern. translation now maps
 * straight through for the wrong-answer hint.
 */
fun VocabularyQuestionDto.toDomainQuestion(): Question = Question(
    id = id,
    type = GameType.valueOf(type),
    prompt = prompt,
    imageUrl = imageUrl,
    options = options.map { AnswerOption(id = it, text = it) },
    correctAnswerId = correctAnswer,
    translation = translation
)
