package com.example.germanclash.data.repository

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.core.result.Result
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import com.example.germanclash.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

/**
 * Picks a transport by room ID prefix - simplest possible routing rule.
 * Everything above this class (use cases, ViewModel, UI) only ever sees
 * GameRepository and has no idea Firestore or Nearby Connections exist.
 */
class GameRepositoryImpl(
    private val onlineSource: MultiplayerDataSource,
    private val offlineSource: MultiplayerDataSource,
    private val soloSource: MultiplayerDataSource
) : GameRepository {

    private fun sourceFor(roomId: String): MultiplayerDataSource = when {
        roomId.startsWith(SOLO_ROOM_PREFIX) -> soloSource
        roomId.startsWith(LOCAL_ROOM_PREFIX) -> offlineSource
        else -> onlineSource
    }

    override fun observeSession(roomId: String): Flow<GameSession> =
        sourceFor(roomId).observeSession(roomId)

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult =
        sourceFor(roomId).submitAnswer(roomId, answerId)

    override suspend fun joinRoom(roomId: String, playerId: String): Result<Unit> =
        if (sourceFor(roomId).joinRoom(roomId, playerId)) {
            Result.Success(Unit)
        } else {
            Result.Error("Could not join room $roomId")
        }

    override suspend fun advanceToNextQuestion(roomId: String) =
        sourceFor(roomId).advanceToNextQuestion(roomId)

    override suspend fun startMatch(roomId: String) =
        sourceFor(roomId).startMatch(roomId)

    override suspend fun toggleReady(roomId: String, playerId: String, isReady: Boolean) =
        sourceFor(roomId).toggleReady(roomId, playerId, isReady)

    companion object {
        private const val LOCAL_ROOM_PREFIX = "LOCAL_"
        private const val SOLO_ROOM_PREFIX = "SOLO_"
    }
}
