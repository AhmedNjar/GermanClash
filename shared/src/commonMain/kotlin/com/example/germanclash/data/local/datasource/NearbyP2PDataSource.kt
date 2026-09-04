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
    
    // Tracks who has answered the CURRENT question - reset on every advanceToNextQuestion
    private val answeredPlayerIds = mutableSetOf<String>()

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
        
        session.value = session.value.copy(
            roomId = roomId,
            players = session.value.players.ifEmpty {
                listOf(Player(id = playerId, displayName = if (isHost) "You (host)" else "You"))
            },
            currentQuestion = null,
            timeRemainingMs = NEARBY_TIME_LIMIT_MS,
            timeLimitMs = NEARBY_TIME_LIMIT_MS,
            isFinished = false,
            currentQuestionNumber = 0
        )

        if (isHost) {
            connectionClient.startAdvertising(roomName = roomId)
        } else {
            if (connectedEndpoints.isEmpty()) {
                connectionClient.startDiscovery { endpointId ->
                    scope.launch { connectionClient.requestConnection(endpointId, localDisplayName = playerId) }
                }
            }
        }
        return true
    }

    override suspend fun startMatch(roomId: String) {
        if (!isHost) return
        answeredPlayerIds.clear()
        session.value = session.value.copy(
            currentQuestion = questionBank.nextQuestion(previousId = null, filter = PracticeFilter()),
            sessionLength = NEARBY_SESSION_LENGTH,
            currentQuestionNumber = 1,
            timeRemainingMs = NEARBY_TIME_LIMIT_MS,
            isFinished = false
        )
        broadcastSessionUpdate()
    }

    override suspend fun toggleReady(roomId: String, playerId: String, isReady: Boolean) {
        if (isHost) {
            updatePlayerReadyStatus(playerId, isReady)
            checkAllPlayersReadyAndStart()
        } else {
            val hostEndpointId = connectedEndpoints.firstOrNull() ?: return
            connectionClient.sendPayload(hostEndpointId, codec.encode(P2PMessage.ReadyStatusChanged(playerId, isReady)))
        }
    }

    private fun updatePlayerReadyStatus(playerId: String, isReady: Boolean) {
        session.value = session.value.copy(
            players = session.value.players.map { player ->
                if (player.id == playerId) player.copy(isReady = isReady) else player
            }
        )
        broadcastSessionUpdate()
    }

    private fun checkAllPlayersReadyAndStart() {
        val players = session.value.players
        // Only auto-start if there's at least one guest (or if it's solo-ish but in P2P mode)
        if (players.size > 1 && players.all { it.isReady }) {
            scope.launch { startMatch(session.value.roomId) }
        }
    }

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult {
        val playerId = localPlayerId.orEmpty()
        val question = session.value.currentQuestion

        if (isHost) {
            val result = scoreLocally(playerId, answerId, question)
            answeredPlayerIds.add(playerId)
            checkAllAnsweredAndAdvance()
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

    private fun checkAllAnsweredAndAdvance() {
        if (!isHost) return
        val connectedPlayerIds = session.value.players.filter { it.isConnected }.map { it.id }.toSet()
        if (answeredPlayerIds.containsAll(connectedPlayerIds)) {
            // Everyone answered! Advance.
            scope.launch { advanceToNextQuestion(session.value.roomId) }
        }
    }

    override suspend fun advanceToNextQuestion(roomId: String) {
        if (!isHost) return
        
        answeredPlayerIds.clear()
        val nextNumber = session.value.currentQuestionNumber + 1
        val length = session.value.sessionLength ?: NEARBY_SESSION_LENGTH
        
        if (nextNumber > length) {
            session.value = session.value.copy(isFinished = true)
        } else {
            session.value = session.value.copy(
                currentQuestion = questionBank.nextQuestion(
                    previousId = session.value.currentQuestion?.id,
                    filter = PracticeFilter()
                ),
                currentQuestionNumber = nextNumber,
                timeRemainingMs = NEARBY_TIME_LIMIT_MS
            )
        }
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
                    
                    answeredPlayerIds.add(message.playerId)
                    checkAllAnsweredAndAdvance()

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
            is P2PMessage.ReadyStatusChanged -> {
                if (isHost) {
                    updatePlayerReadyStatus(message.playerId, message.isReady)
                    checkAllPlayersReadyAndStart()
                }
            }
        }
    }

    private fun handleConnectionEvent(event: P2PConnectionEvent) {
        when (event) {
            is P2PConnectionEvent.Connected -> {
                connectedEndpoints += event.endpointId
                if (!isHost) {
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
                        session.value = session.value.copy(
                            players = session.value.players.map { player ->
                                if (player.id == leftPlayerId) player.copy(isConnected = false) else player
                            }
                        )
                        broadcastSessionUpdate()
                        // If someone leaves, maybe re-check if everyone remaining is ready
                        checkAllPlayersReadyAndStart()
                        // Or check if everyone remaining has answered
                        checkAllAnsweredAndAdvance()
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
