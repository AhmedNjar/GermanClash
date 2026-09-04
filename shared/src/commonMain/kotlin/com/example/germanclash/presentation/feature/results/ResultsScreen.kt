package com.example.germanclash.presentation.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.germanclash.presentation.common.AnswerState
import com.example.germanclash.presentation.common.ConfettiOverlay
import com.example.germanclash.presentation.common.JuicyButton
import com.example.germanclash.presentation.common.SectionCard

private const val CONFETTI_ACCURACY_THRESHOLD = 70

@Composable
fun ResultsScreen(
    score: Int,
    correctCount: Int,
    totalCount: Int,
    dailyBestScore: Int? = null,
    isMultiplayer: Boolean = false,
    matchPlayers: List<Pair<String, Int>> = emptyList(),
    onPlayAgain: () -> Unit,
    onChangeCategory: () -> Unit
) {
    val accuracyPercent = if (totalCount > 0) (correctCount * 100 / totalCount) else 0
    val message = when {
        totalCount == 0 -> "Session complete"
        accuracyPercent >= 90 -> "Amazing! \uD83C\uDF1F"
        accuracyPercent >= 70 -> "Great job! \uD83D\uDC4F"
        accuracyPercent >= 50 -> "Good effort! \uD83D\uDCAA"
        else -> "Keep practicing! \uD83D\uDCDA"
    }

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        if (accuracyPercent >= CONFETTI_ACCURACY_THRESHOLD) confettiTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            SectionCard(title = "Score", modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(text = "$score points", fontWeight = FontWeight.Bold)
            }

            SectionCard(title = "Accuracy", modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(text = "$correctCount / $totalCount correct ($accuracyPercent%)")
            }

            if (dailyBestScore != null) {
                SectionCard(title = "Daily Challenge", modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text(
                        text = if (score >= dailyBestScore) {
                            "New best today: $dailyBestScore points! \uD83C\uDFC6"
                        } else {
                            "Your best today: $dailyBestScore points"
                        }
                    )
                }
            }

            // Only meaningful with more than one real player - a "leaderboard"
            // of just yourself is clutter, not a feature.
            if (matchPlayers.size > 1) {
                SectionCard(title = "Match Results", modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    matchPlayers.sortedByDescending { it.second }.forEachIndexed { index, (name, playerScore) ->
                        Text(text = "${index + 1}. $name - $playerScore")
                    }
                }
            } else {
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }

            JuicyButton(
                text = if (isMultiplayer) "Rematch" else "Play again",
                state = AnswerState.IDLE,
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            JuicyButton(
                text = "Change category",
                state = AnswerState.IDLE,
                onClick = onChangeCategory,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ConfettiOverlay(triggerKey = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}

@Composable
@Preview
fun ResultsScreenPreview() {
    ResultsScreen(
        score = 850,
        correctCount = 8,
        totalCount = 10,
        dailyBestScore = 1200,
        isMultiplayer = false,
        onPlayAgain = {},
        onChangeCategory = {}
    )
}
