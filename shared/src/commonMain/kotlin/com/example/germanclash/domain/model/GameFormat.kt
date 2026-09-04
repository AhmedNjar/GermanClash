package com.example.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameFormat {
    CLASSIC,
    MIXED,
    SPEED,
    DAILY
}
