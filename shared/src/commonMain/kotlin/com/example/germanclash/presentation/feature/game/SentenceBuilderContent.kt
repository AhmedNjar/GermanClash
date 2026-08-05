package com.example.germanclash.presentation.feature.game

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.germanclash.presentation.theme.GameColors

/**
 * Duolingo-style tap-to-build sentence: tap a word from the bank to place
 * it, tap the assembled area to undo the last one. Kept to taps instead of
 * drag gestures - same end result, much less state to manage.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderContent(
    wordBank: List<String>,
    assembledWords: List<String>,
    isLocked: Boolean,
    onWordTapped: (String) -> Unit,
    onRemoveLastTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GameColors.OptionDefault)
                .clickable(enabled = assembledWords.isNotEmpty() && !isLocked) { onRemoveLastTapped() }
                .padding(12.dp)
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            assembledWords.forEach { word ->
                WordChip(word = word, background = GameColors.OptionSelected)
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            wordBank.forEach { word ->
                WordChip(
                    word = word,
                    background = GameColors.OptionDefault,
                    onClick = if (isLocked) null else ({ onWordTapped(word) })
                )
            }
        }
    }
}

@Composable
private fun WordChip(
    word: String,
    background: Color,
    onClick: (() -> Unit)? = null
) {
    Text(
        text = word,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
