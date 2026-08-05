package com.example.germanclash.data.local.questionbank

import kotlinx.serialization.Serializable

@Serializable
data class VocabularyQuestionDto(
    val id: String,
    val type: String,
    val category: String? = null,
    val prompt: String,
    val imageUrl: String? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val translation: String? = null
)