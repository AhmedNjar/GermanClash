package com.example.germanclash.core.contracts

import kotlinx.coroutines.flow.Flow

/**
 * commonMain only ever talks to this interface. The Android implementation
 * (data/local/nearby/AndroidP2PConnectionClient, in androidMain) is the one
 * place that touches the real Nearby Connections API.
 */
interface P2PConnectionClient {
    fun incomingPayloads(): Flow<ByteArray>
    suspend fun sendPayload(endpointId: String, bytes: ByteArray)
    suspend fun startAdvertising(roomName: String)
    suspend fun startDiscovery(onEndpointFound: (endpointId: String) -> Unit)
    fun disconnect()
}