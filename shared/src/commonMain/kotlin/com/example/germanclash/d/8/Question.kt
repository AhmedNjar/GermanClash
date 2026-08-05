package com.app.germanclash.domain.model

import kotlinx.serialization.Serializable

/**
 * One generic Question shape shared by all 6 mini-games.
 * Only the fields relevant to `type` are populated - keeps the domain
 * layer at a single model instead of six near-duplicate classes.
 */
@Serializable
data class Question(
    val id: String,
    val type: GameType,
    val prompt: String,                              // e.g. "___ Hund", "Select the odd one out"
    val imageUrl: String? = null,
    val options: List<AnswerOption> = emptyList(),    // DER_DIE_DAS, IMAGE_GUESS, ODD_ONE_OUT, WORD_COMPLETION
    val scrambledWords: List<String> = emptyList(),   // SENTENCE_BUILDER
    val cardPairs: List<CardPair> = emptyList(),      // MATCH_PAIRS
    val correctAnswerId: String? = null,
    val translation: String? = null                  // shown as a hint after a wrong answer
)

@Serializable
data class AnswerOption(
    val id: String,
    val text: String
)

@Serializable
data class CardPair(
    val id: String,
    val front: String,   // German word
    val back: String     // translation or image keyword
)
