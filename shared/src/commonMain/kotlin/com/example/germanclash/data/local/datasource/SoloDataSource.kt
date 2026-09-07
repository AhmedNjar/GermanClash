package com.example.germanclash.data.local.datasource

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.data.local.questionbank.PracticeFilterState
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
 * no Nearby Connections. Questions come from QuestionBank, filtered by
 * whatever PracticeFilterState was set on ModeSelectScreen, and every
 * answer is scored immediately, in memory.
 */
class SoloDataSource(
    private val questionBank: QuestionBank,
    private val practiceFilterState: PracticeFilterState
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
                currentQuestion = questionBank.nextQuestion(previousId = null, filter = practiceFilterState.current),
                sessionLength = practiceFilterState.current.sessionLength
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
        // currentQuestion is deliberately left unchanged here - the caller
        // shows the result against THIS question, then calls
        // advanceToNextQuestion() itself once that's had a moment on screen.
        session.value = session.value.copy(players = updatedPlayers)

        return RoundResult(
            wasCorrect = wasCorrect,
            correctAnswerId = question?.correctAnswerId.orEmpty(),
            pointsAwarded = pointsAwarded
        )
    }

    override suspend fun joinRoom(roomId: String, playerId: String): Boolean = true

    override suspend fun advanceToNextQuestion(roomId: String) {
        session.value = session.value.copy(
            currentQuestion = questionBank.nextQuestion(
                previousId = session.value.currentQuestion?.id,
                filter = practiceFilterState.current
            ),
            timeRemainingMs = SOLO_TIME_LIMIT_MS
        )
    }

    override suspend fun startMatch(roomId: String) {
        // No-op: Solo has no lobby to wait for a "ready" tap in - it starts
        // itself the moment observeSession() is first called.
    }

    override suspend fun toggleReady(roomId: String, playerId: String, isReady: Boolean) {
        // No-op for solo
    }

    override suspend fun updateSettings(
        roomId: String,
        format: com.example.germanclash.domain.model.GameFormat,
        timeLimitMs: Long,
        category: String?
    ) {
        // No-op for solo, settings are handled via PracticeFilterState for now
    }
}
