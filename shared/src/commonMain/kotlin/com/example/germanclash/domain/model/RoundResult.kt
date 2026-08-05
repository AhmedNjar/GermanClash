package com.example.germanclash.domain.model

data class RoundResult(
    val wasCorrect: Boolean,
    val correctAnswerId: String,
    val pointsAwarded: Int
)