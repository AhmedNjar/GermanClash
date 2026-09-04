package com.example.germanclash.data.local.serialization

import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import kotlinx.serialization.Serializable

/**
 * NOTE: GameSession is annotated @Serializable at its declaration so it can
 * travel raw over P2P without a separate DTO - a deliberate simplification
 * since, unlike Firestore, there's no server wire-format to isolate from.
 */
@Serializable
sealed interface P2PMessage {
    @Serializable
    data class SessionUpdate(val session: GameSession) : P2PMessage

    @Serializable
    data class AnswerSubmitted(val playerId: String, val answerId: String) : P2PMessage

    @Serializable
    data class Hello(val playerId: String, val displayName: String) : P2PMessage

    @Serializable
    data class RoundResultMessage(
        val playerId: String,
        val questionId: String,
        val result: RoundResult
    ) : P2PMessage

    @Serializable
    data class ReadyStatusChanged(val playerId: String, val isReady: Boolean) : P2PMessage
}
