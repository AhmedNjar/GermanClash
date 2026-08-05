package com.example.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RoundResult(
    val wasCorrect: Boolean,
    val correctAnswerId: String,
    val pointsAwarded: Int
)
