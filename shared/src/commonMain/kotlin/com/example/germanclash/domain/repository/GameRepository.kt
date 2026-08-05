package com.example.germanclash.domain.repository

import com.example.germanclash.core.result.Result
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract. The implementation (data layer) decides whether
 * a given roomId is played online (Firestore) or offline (Nearby P2P) -
 * this interface, and everything above it, never needs to know which.
 */
interface GameRepository {
    fun observeSession(roomId: String): Flow<GameSession>
    suspend fun submitAnswer(roomId: String, answerId: String): RoundResult
    suspend fun joinRoom(roomId: String, playerId: String): Result<Unit>
}