package com.app.germanclash.core.contracts

import com.app.germanclash.domain.model.GameSession
import com.app.germanclash.domain.model.RoundResult
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

    /**
     * Moves the session on to its next question. Meaningful for SoloDataSource
     * and NearbyP2PDataSource (both act as their own host); Firestore is a
     * no-op since round progression there is driven by the Cloud Function,
     * not by this device.
     */
    suspend fun advanceToNextQuestion(roomId: String)

    /**
     * Begins the match - generates the first question and sets sessionLength.
     * Called once, explicitly, when the player marks themselves ready in the
     * Room lobby - not on every observeSession() subscription, since RoomScreen
     * also calls observeSession() to show the player list and shouldn't jump
     * straight into a question before anyone's ready. Solo has no lobby, so it
     * starts itself in observeSession() and this is a no-op there.
     */
    suspend fun startMatch(roomId: String)
}
