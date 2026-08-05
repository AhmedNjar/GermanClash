package com.example.germanclash

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.germanclash.navigation.GermanClashNavHost
import com.example.germanclash.presentation.common.AndroidSoundEffectPlayer
import com.example.germanclash.presentation.theme.GermanClashBackground
import com.example.germanclash.presentation.theme.GermanClashTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    // Nearby Connections needs these granted at RUNTIME, not just declared in the
    // manifest - without this, startAdvertising/startDiscovery fail silently or throw.
    private val requestNearbyPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* no rationale UI yet - if denied, hosting/joining will just silently fail to connect */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNearbyPermissions.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        )

        setContent {
            val soundPlayer = remember { AndroidSoundEffectPlayer(applicationContext) }
            // Placeholder - replace with the real signed-in user ID (Firebase Auth) once
            // that UI exists. Host/Join room IDs are now generated in GermanClashNavHost.
            val localPlayerId = remember { UUID.randomUUID().toString() }

            GermanClashTheme {
                // Surface (not just a bare Box) so it sets LocalContentColor to
                // onBackground = White for every descendant Text by default -
                // GermanClashBackground's gradient then paints over it visually.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GermanClashBackground {
                        GermanClashNavHost(
                            localPlayerId = localPlayerId,
                            soundPlayer = soundPlayer
                        )
                    }
                }
            }
        }
    }
}
