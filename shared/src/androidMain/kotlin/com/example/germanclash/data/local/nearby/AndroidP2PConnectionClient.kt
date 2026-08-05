package com.example.germanclash.data.local.nearby

import android.content.Context
import com.example.germanclash.core.contracts.IncomingPayload
import com.example.germanclash.core.contracts.P2PConnectionClient
import com.example.germanclash.core.contracts.P2PConnectionEvent
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Android implementation of P2PConnectionClient using Google Nearby Connections.
 * Handles the mapping between Nearby's callback-based API and the shared Flow-based contract.
 */
class AndroidP2PConnectionClient(
    context: Context
) : P2PConnectionClient {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val incoming = MutableSharedFlow<IncomingPayload>(extraBufferCapacity = 16)
    private val events = MutableSharedFlow<P2PConnectionEvent>(extraBufferCapacity = 16)

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes -> 
                incoming.tryEmit(IncomingPayload(endpointId, bytes)) 
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Auto-accept every incoming connection - the hello handshake
            // handled at the DataSource layer validates the playerId.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                events.tryEmit(P2PConnectionEvent.Connected(endpointId))
            }
        }

        override fun onDisconnected(endpointId: String) {
            events.tryEmit(P2PConnectionEvent.Disconnected(endpointId))
        }
    }

    override fun incomingPayloads(): Flow<IncomingPayload> = incoming
    override fun connectionEvents(): Flow<P2PConnectionEvent> = events

    override suspend fun requestConnection(endpointId: String, localDisplayName: String) {
        connectionsClient.requestConnection(localDisplayName, endpointId, connectionLifecycleCallback)
    }

    override suspend fun sendPayload(endpointId: String, bytes: ByteArray) {
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    override suspend fun startAdvertising(roomName: String) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startAdvertising(roomName, SERVICE_ID, connectionLifecycleCallback, options)
    }

    override suspend fun startDiscovery(onEndpointFound: (endpointId: String) -> Unit) {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    onEndpointFound(endpointId)
                }
                override fun onEndpointLost(endpointId: String) = Unit
            },
            options
        )
    }

    override fun disconnect() {
        connectionsClient.stopAllEndpoints()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
    }

    companion object {
        private const val SERVICE_ID = "com.example.germanclash"
    }
}
