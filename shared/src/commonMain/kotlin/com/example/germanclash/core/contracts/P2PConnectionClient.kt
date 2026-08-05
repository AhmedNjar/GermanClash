package com.example.germanclash.core.contracts

import kotlinx.coroutines.flow.Flow

/**
 * commonMain only ever talks to this interface. The Android implementation
 * (data/local/nearby/AndroidP2PConnectionClient, in androidMain) is the one
 * place that touches the real Nearby Connections API.
 */
interface P2PConnectionClient {
    fun incomingPayloads(): Flow<IncomingPayload>
    fun connectionEvents(): Flow<P2PConnectionEvent>
    suspend fun requestConnection(endpointId: String, localDisplayName: String)
    suspend fun sendPayload(endpointId: String, bytes: ByteArray)
    suspend fun startAdvertising(roomName: String)
    suspend fun startDiscovery(onEndpointFound: (endpointId: String) -> Unit)
    fun disconnect()
}

data class IncomingPayload(val endpointId: String, val bytes: ByteArray)

sealed interface P2PConnectionEvent {
    data class Connected(val endpointId: String) : P2PConnectionEvent
    data class Disconnected(val endpointId: String) : P2PConnectionEvent
}
