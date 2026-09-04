package com.example.germanclash.data.remote.datasource

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.data.remote.dto.GameSessionDto
import com.example.germanclash.data.remote.mapper.GameSessionMapper
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * Online transport. Scoring happens server-side (a Cloud Function reads the
 * "answers" subcollection, decides correctness, and writes the result back
 * onto the session doc) - this class only ever reads/writes Firestore,
 * it never scores an answer itself.
 */
class FirestoreDataSource(
    private val mapper: GameSessionMapper
) : MultiplayerDataSource {

    private val sessions = Firebase.firestore.collection("game_sessions")

    override fun observeSession(roomId: String): Flow<GameSession> =
        sessions.document(roomId).snapshots.map { snapshot ->
            snapshot.data<GameSessionDto?>()
                ?.let { mapper.toDomain(it) }
                ?: GameSession(roomId, emptyList(), null, 0, 0)
        }

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult {
        val playerId = Firebase.auth.currentUser?.uid.orEmpty()
        val sessionRef = sessions.document(roomId)

        val questionId = sessionRef.get().data<GameSessionDto?>()?.currentQuestion?.id

        sessionRef.collection("answers").document(playerId)
            .set(mapper.toAnswerDto(playerId, answerId))

        // scoreAnswer (Cloud Function) scores the answer and writes
        // lastResults.{playerId} onto this same doc - wait for that specific
        // entry, checking questionId so a leftover result from the previous
        // round can't be mistaken for this one's.
        return sessionRef.snapshots
            .mapNotNull { snapshot -> snapshot.data<GameSessionDto?>()?.lastResults?.get(playerId) }
            .mapNotNull { resultDto -> resultDto.takeIf { it.questionId == questionId } }
            .map { resultDto -> mapper.toRoundResult(resultDto) }
            .first()
    }

    override suspend fun joinRoom(roomId: String, playerId: String): Boolean = try {
        sessions.document(roomId).collection("players").document(playerId)
            .set(mapOf("id" to playerId, "score" to 0, "isReady" to true))
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun advanceToNextQuestion(roomId: String) {
        // No-op online: the next question is chosen server-side (Cloud Function
        // or host client) and arrives through observeSession() for everyone.
    }

    override suspend fun startMatch(roomId: String) {
        // No-op: a real online host loop needs a Cloud Function (or a
        // designated host client) to write the first currentQuestion into
        // Firestore - that doesn't exist yet, and this transport isn't
        // currently reachable from the app's UI regardless.
    }

    override suspend fun toggleReady(roomId: String, playerId: String, isReady: Boolean) {
        // In a real Firestore implementation, this would update the player's 
        // ready status in the "players" subcollection.
        sessions.document(roomId).collection("players").document(playerId)
            .update("isReady" to isReady)
    }
}
