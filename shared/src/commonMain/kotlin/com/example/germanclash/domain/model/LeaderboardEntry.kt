package com.example.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val score: Int,
    val isCurrentUser: Boolean = false,
    val avatarUrl: String? = null
)
