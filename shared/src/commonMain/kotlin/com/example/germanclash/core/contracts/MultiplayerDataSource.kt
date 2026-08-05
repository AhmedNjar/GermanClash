package com.example.germanclash.core.contracts

import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import kotlinx.coroutines.flow.Flow

/**
 * Transport-agnostic contract. GameRepositoryImpl is the only class that
 * knows two implementations of this exist (Firestore vs Nearby P2P) -
 * domain and presentation layers only ever see GameRepository.
 */
interface MultiplayerDataSource {
    fun observeSession(roomId: String): Flow<GameSession>
    suspend fun submitAnswer(roomId: String, answerId: String): RoundResult
    suspend fun joinRoom(roomId: String, playerId: String): Boolean
}