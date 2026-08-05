package com.example.germanclash.data.local.datasource

import com.example.germanclash.core.contracts.IncomingPayload
import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.core.contracts.P2PConnectionClient
import com.example.germanclash.core.contracts.P2PConnectionEvent
import com.example.germanclash.data.local.questionbank.PracticeFilter
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.data.local.serialization.P2PMessage
import com.example.germanclash.data.local.serialization.P2PMessageCodec
import com.example.germanclash.domain.model.ConnectionStatus
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.Player
import com.example.germanclash.domain.model.Question
import com.example.germanclash.domain.model.RoundResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val NEARBY_TIME_LIMIT_MS = 10_000L
private const val NEARBY_POINTS_CORRECT = 100
private const val NEARBY_SESSION_LENGTH = 10
private const val HOST_MARKER = "_HOST_"

/**
 * Offline transport - now a real, if simple, two-role protocol instead of a
 * single-device stub:
 *
 * - HOST (roomId contains "_HOST_"): advertises, owns the QuestionBank,
 *   scores every answer (including its own), and broadcasts the full
 *   GameSession to every connected guest whenever anything changes. If a
 *   guest's connection drops, the host marks their Player isConnected = false
 *   but keeps their entry (and score) rather than removing them, so a
 *   reconnect picks up where they left off instead of starting over.
 * - GUEST (roomId contains "_JOIN_"): discovers a host, requests a
 *   connection, sends a Hello so the host knows its real playerId, then
 *   treats every SessionUpdate from the host as authoritative. Submitting
 *   an answer sends it to the host and suspends until that exact answer's
 *   RoundResultMessage comes back - the same "wait for the real result"
 *   shape FirestoreDataSource uses. If the connection to the host drops,
 *   the guest flags connectionStatus = RECONNECTING and restarts discovery
 *   automatically; the same stable localPlayerId in its next Hello is what
 *   lets the host recognize it as a returning player, not a stranger.
 *
 * Known gaps, deliberately left for later: no host migration if the HOST
 * itself disconnects (there's no election/mesh topology for guests to fall
 * back on - this only covers a guest losing its link to a still-alive host),
 * and a guest always connects to the FIRST endpoint it discovers (no picker
 * for multiple nearby hosts).
 */
class NearbyP2PDataSource(
    private val connectionClient: P2PConnectionClient,
    private val codec: P2PMessageCodec,
    private val questionBank: QuestionBank,
    private val scope: CoroutineScope
) : MultiplayerDataSource {

    private val session = MutableStateFlow(
        GameSession(
            roomId = "",
            players = emptyList(),
            currentQuestion = null,
            timeRemainingMs = NEARBY_TIME_LIMIT_MS,
            timeLimitMs = NEARBY_TIME_LIMIT_MS
        )
    )

    private var localPlayerId: String? = null
    private var isHost = false
    private val connectedEndpoints = mutableSetOf<String>()
    private val endpointToPlayerId = mutableMapOf<String, String>()

    // replay > 0 so a RoundResultMessage that arrives just before submitAnswer's
    // .first() subscribes isn't lost - the filter below still guarantees we only
    // ever act on the one that actually matches this exact answer.
    private val incomingRoundResults =
        MutableSharedFlow<P2PMessage.RoundResultMessage>(replay = 4, extraBufferCapacity = 8)

    init {
        connectionClient.incomingPayloads()
            .onEach { incoming -> handleIncomingPayload(incoming) }
            .launchIn(scope)

        connectionClient.connectionEvents()
            .onEach { event -> handleConnectionEvent(event) }
            .launchIn(scope)
    }

    override fun observeSession(roomId: String): Flow<GameSession> = session.asStateFlow()

    override suspend fun joinRoom(roomId: String, playerId: String): Boolean {
        localPlayerId = playerId
        isHost = roomId.contains(HOST_MARKER)
        connectedEndpoints.clear()
        endpointToPlayerId.clear()

        session.value = GameSession(
            roomId = roomId,
            players = listOf(Player(id = playerId, displayName = if (isHost) "You (host)" else "You")),
            currentQuestion = null,
            timeRemainingMs = NEARBY_TIME_LIMIT_MS,
            timeLimitMs = NEARBY_TIME_LIMIT_MS
        )

        if (isHost) {
            connectionClient.startAdvertising(roomName = roomId)
        } else {
            connectionClient.startDiscovery { endpointId ->
                scope.launch { connectionClient.requestConnection(endpointId, localDisplayName = playerId) }
            }
        }
        return true
    }

    override suspend fun startMatch(roomId: String) {
        if (!isHost || session.value.currentQuestion != null) return
        session.value = session.value.copy(
            currentQuestion = questionBank.nextQuestion(previousId = null, filter = PracticeFilter()),
            sessionLength = NEARBY_SESSION_LENGTH,
            timeRemainingMs = NEARBY_TIME_LIMIT_MS
        )
        broadcastSessionUpdate()
    }

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult {
        val playerId = localPlayerId.orEmpty()
        val question = session.value.currentQuestion

        if (isHost) {
            val result = scoreLocally(playerId, answerId, question)
            broadcastSessionUpdate()
            return result
        }

        val hostEndpointId = connectedEndpoints.firstOrNull()
            ?: return RoundResult(wasCorrect = false, correctAnswerId = question?.correctAnswerId.orEmpty(), pointsAwarded = 0)

        connectionClient.sendPayload(hostEndpointId, codec.encode(P2PMessage.AnswerSubmitted(playerId, answerId)))

        return incomingRoundResults
            .filter { it.playerId == playerId && it.questionId == question?.id }
            .map { it.result }
            .first()
    }

    override suspend fun advanceToNextQuestion(roomId: String) {
        if (!isHost) return // the guest just waits for the host's next SessionUpdate
        session.value = session.value.copy(
            currentQuestion = questionBank.nextQuestion(
                previousId = session.value.currentQuestion?.id,
                filter = PracticeFilter()
            ),
            timeRemainingMs = NEARBY_TIME_LIMIT_MS
        )
        broadcastSessionUpdate()
    }

    private fun scoreLocally(playerId: String, answerId: String, question: Question?): RoundResult {
        val wasCorrect = question?.correctAnswerId == answerId
        val pointsAwarded = if (wasCorrect) NEARBY_POINTS_CORRECT else 0
        session.value = session.value.copy(
            players = session.value.players.map { player ->
                if (player.id == playerId) player.copy(score = player.score + pointsAwarded) else player
            }
        )
        return RoundResult(
            wasCorrect = wasCorrect,
            correctAnswerId = question?.correctAnswerId.orEmpty(),
            pointsAwarded = pointsAwarded
        )
    }

    private fun handleIncomingPayload(incoming: IncomingPayload) {
        when (val message = codec.decode(incoming.bytes)) {
            is P2PMessage.SessionUpdate -> {
                if (!isHost) session.value = message.session
            }
            is P2PMessage.Hello -> {
                if (isHost) {
                    endpointToPlayerId[incoming.endpointId] = message.playerId
                    val existingPlayer = session.value.players.find { it.id == message.playerId }
                    session.value = session.value.copy(
                        players = when {
                            existingPlayer != null -> session.value.players.map { player ->
                                if (player.id == message.playerId) player.copy(isConnected = true) else player
                            }
                            else -> session.value.players + Player(
                                id = message.playerId,
                                displayName = "Player ${session.value.players.size + 1}"
                            )
                        }
                    )
                    broadcastSessionUpdate()
                }
            }
            is P2PMessage.AnswerSubmitted -> {
                if (isHost) {
                    val question = session.value.currentQuestion
                    val result = scoreLocally(message.playerId, message.answerId, question)
                    broadcastSessionUpdate()
                    scope.launch {
                        connectionClient.sendPayload(
                            incoming.endpointId,
                            codec.encode(P2PMessage.RoundResultMessage(message.playerId, question?.id.orEmpty(), result))
                        )
                    }
                }
            }
            is P2PMessage.RoundResultMessage -> {
                incomingRoundResults.tryEmit(message)
            }
        }
    }

    private fun handleConnectionEvent(event: P2PConnectionEvent) {
        when (event) {
            is P2PConnectionEvent.Connected -> {
                connectedEndpoints += event.endpointId
                if (!isHost) {
                    // Tell the host who we really are - endpointId isn't a
                    // shared identifier between the two sides of a connection.
                    val myId = localPlayerId.orEmpty()
                    scope.launch {
                        connectionClient.sendPayload(event.endpointId, codec.encode(P2PMessage.Hello(myId, "Player")))
                    }
                }
            }
            is P2PConnectionEvent.Disconnected -> {
                connectedEndpoints -= event.endpointId

                if (isHost) {
                    val leftPlayerId = endpointToPlayerId.remove(event.endpointId)
                    if (leftPlayerId != null) {
                        // Keep the player (and their score) rather than removing them -
                        // Hello on a reconnect flips isConnected back to true in place.
                        session.value = session.value.copy(
                            players = session.value.players.map { player ->
                                if (player.id == leftPlayerId) player.copy(isConnected = false) else player
                            }
                        )
                        broadcastSessionUpdate()
                    }
                } else {
                    session.value = session.value.copy(connectionStatus = ConnectionStatus.RECONNECTING)
                    scope.launch {
                        connectionClient.startDiscovery { endpointId ->
                            scope.launch {
                                connectionClient.requestConnection(endpointId, localDisplayName = localPlayerId.orEmpty())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun broadcastSessionUpdate() {
        if (!isHost) return
        val bytes = codec.encode(P2PMessage.SessionUpdate(session.value))
        scope.launch {
            connectedEndpoints.forEach { endpointId -> connectionClient.sendPayload(endpointId, bytes) }
        }
    }
}
