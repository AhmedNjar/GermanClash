package com.example.germanclash.data.local.datasource

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.core.contracts.P2PConnectionClient
import com.example.germanclash.data.local.serialization.P2PMessage
import com.example.germanclash.data.local.serialization.P2PMessageCodec
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.RoundResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Offline transport. One connected device acts as "host" for scoring - the
 * simplest sync model for a 2-4 player local match, no server required.
 * Every payload received is a full GameSession snapshot broadcast by the host.
 */
class NearbyP2PDataSource(
    private val connectionClient: P2PConnectionClient,
    private val codec: P2PMessageCodec,
    private val scope: CoroutineScope
) : MultiplayerDataSource {

    private val localSession = MutableStateFlow<GameSession?>(null)

    init {
        connectionClient.incomingPayloads()
            .onEach { bytes ->
                when (val message = codec.decode(bytes)) {
                    is P2PMessage.SessionUpdate -> localSession.value = message.session
                    is P2PMessage.AnswerSubmitted -> Unit // host-side scoring hook, wired when host role is built
                }
            }
            .launchIn(scope)
    }

    override fun observeSession(roomId: String): Flow<GameSession> =
        localSession.map { it ?: GameSession(roomId, emptyList(), null, 0, 0) }

    override suspend fun submitAnswer(roomId: String, answerId: String): RoundResult {
        val playerId = localSession.value?.players?.firstOrNull()?.id.orEmpty()
        val bytes = codec.encode(P2PMessage.AnswerSubmitted(playerId, answerId))
        localSession.value?.players?.forEach { player ->
            connectionClient.sendPayload(player.id, bytes)
        }
        // Host peer scores and broadcasts a SessionUpdate; this device
        // reconciles via the incomingPayloads collector above.
        return RoundResult(wasCorrect = false, correctAnswerId = "", pointsAwarded = 0)
    }

    override suspend fun joinRoom(roomId: String, playerId: String): Boolean {
        connectionClient.startDiscovery { /* endpointId */ }
        return true
    }
}