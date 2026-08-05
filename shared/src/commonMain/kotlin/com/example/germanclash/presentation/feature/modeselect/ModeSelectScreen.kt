package com.example.germanclash.presentation.feature.modeselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.germanclash.presentation.common.AnswerState
import com.example.germanclash.presentation.common.JuicyButton

@Composable
@Preview
fun ModeSelectScreen(
    onPlaySolo: () -> Unit = {},
    onPlayWithFriends: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "GermanClash", modifier = Modifier.padding(bottom = 32.dp))

        JuicyButton(
            text = "Play solo",
            state = AnswerState.IDLE,
            onClick = onPlaySolo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        JuicyButton(
            text = "Play with friends",
            state = AnswerState.IDLE,
            onClick = onPlayWithFriends,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
