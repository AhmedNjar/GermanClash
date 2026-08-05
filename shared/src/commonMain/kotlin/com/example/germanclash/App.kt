package com.example.germanclash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.germanclash.navigation.GermanClashNavHost
import com.example.germanclash.presentation.common.SoundEffectPlayer
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val soundPlayer = koinInject<SoundEffectPlayer>()
            
            // In a real app, these would come from an Auth/Session manager.
            // Using remember ensures they persist across navigation within the same session.
            val localPlayerId = remember { "PLAYER_${(0..99999).random()}" }
            val friendsRoomId = remember { "LOCAL_DEMO_ROOM" }

            GermanClashNavHost(
                friendsRoomId = friendsRoomId,
                localPlayerId = localPlayerId,
                soundPlayer = soundPlayer
            )
        }
    }
}
