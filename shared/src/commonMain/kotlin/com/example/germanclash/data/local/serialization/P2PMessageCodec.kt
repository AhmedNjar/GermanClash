package com.example.germanclash.data.local.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class P2PMessageCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(message: P2PMessage): ByteArray =
        json.encodeToString(P2PMessage.serializer(), message).encodeToByteArray()

    fun decode(bytes: ByteArray): P2PMessage =
        json.decodeFromString(P2PMessage.serializer(), bytes.decodeToString())
}