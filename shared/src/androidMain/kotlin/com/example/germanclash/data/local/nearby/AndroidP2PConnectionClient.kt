package com.example.germanclash.data.local.nearby

import android.content.Context
import com.example.germanclash.core.contracts.P2PConnectionClient
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
 * NOTE: runtime permission requests (BLUETOOTH_ADVERTISE/CONNECT/SCAN,
 * NEARBY_WIFI_DEVICES) are the caller's responsibility - this class assumes
 * they're already granted by the time startAdvertising/startDiscovery runs.
 */
class AndroidP2PConnectionClient(
    context: Context
) : P2PConnectionClient {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val connectedEndpoints = mutableSetOf<String>()
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes -> incoming.tryEmit(bytes) }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) connectedEndpoints += endpointId
        }
        override fun onDisconnected(endpointId: String) {
            connectedEndpoints -= endpointId
        }
    }

    override fun incomingPayloads(): Flow<ByteArray> = incoming

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
        connectedEndpoints.clear()
    }

    companion object {
        private const val SERVICE_ID = "com.example.germanclash"
    }
}