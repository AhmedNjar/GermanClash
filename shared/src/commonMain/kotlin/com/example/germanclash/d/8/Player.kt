package com.app.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val displayName: String,
    val score: Int = 0,
    val isReady: Boolean = false,
    val isConnected: Boolean = true
)
