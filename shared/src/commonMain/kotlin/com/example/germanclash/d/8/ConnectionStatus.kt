package com.app.germanclash.domain.model

import kotlinx.serialization.Serializable

/**
 * Meaningful for a guest's own connection to its host - Solo and Firestore
 * never set this to anything but the default, and a host never sets it to
 * RECONNECTING on itself (it doesn't have an "uplink" the way a guest does).
 */
@Serializable
enum class ConnectionStatus { CONNECTED, RECONNECTING }
