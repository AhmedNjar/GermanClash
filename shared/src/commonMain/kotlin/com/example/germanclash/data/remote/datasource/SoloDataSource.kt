package com.example.germanclash.data.remote.datasource

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.Player
import com.example.germanclash.domain.model.RoundResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val SOLO_TIME_LIMIT_MS = 10_000L
private const val SOLO_POINTS_CORRECT = 100
private const val SOLO_PLAYER_ID = "solo_player"

/**
 * The simplest of the three transports - one local player, no Firestore,
 * no Nearby Connections. Questions come from QuestionBank and every answer
 * is scored immediately, in memory.
 */
class SoloDataSource(
    private val questionBank: QuestionBank
) : MultiplayerDataSource {

    private val session = MutableStateFlow(
        GameSession(
            roomId = "",
            players = listOf(Player(id = SOLO_PLAYER_ID, displayName = "You")),
            currentQuestion = null,
            timeRemainingMs = SOLO_TIME_LIMIT_MS,
            timeLimitMs = SOLO_TIME_LIMIT_MS
        )
    )

    override fun observeSession(roomId: String): Flow<GameSession> {
        if (session.value.currentQuestion == null) {
            session.value = session.value.copy(
                roomId = roomId,
                currentQuestion = questionBank.nextQuestion(previousId = null)
            )
        }
        return session.asStateFlow()
    }

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult {
        val question = session.value.currentQuestion
        val wasCorrect = question?.correctAnswerId == answerId
        val pointsAwarded = if (wasCorrect) SOLO_POINTS_CORRECT else 0

        val updatedPlayers = session.value.players.map { player ->
            if (player.id == SOLO_PLAYER_ID) player.copy(score = player.score + pointsAwarded) else player
        }
        session.value = session.value.copy(
            players = updatedPlayers,
            currentQuestion = questionBank.nextQuestion(previousId = question?.id)
        )

        return RoundResult(
            wasCorrect = wasCorrect,
            correctAnswerId = question?.correctAnswerId.orEmpty(),
            pointsAwarded = pointsAwarded
        )
    }

    override suspend fun joinRoom(roomId: String, playerId: String): Boolean = true
}
