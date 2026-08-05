package com.example.germanclash.presentation.feature.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.germanclash.presentation.theme.GameColors

@Composable
fun MatchPairsContent(
    cards: List<MatchCard>,
    flippedCardIds: List<String>,
    onCardTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            MatchCardView(
                card = card,
                isFaceUp = flippedCardIds.contains(card.id),
                onClick = { onCardTapped(card.id) }
            )
        }
    }
}

@Composable
private fun MatchCardView(
    card: MatchCard,
    isFaceUp: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )
    val showingFace = rotation > 90f

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    card.isMatched -> GameColors.CorrectGreen
                    showingFace -> GameColors.OptionSelected
                    else -> GameColors.OptionDefault
                }
            )
            .clickable(enabled = !isFaceUp && !card.isMatched, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (showingFace) {
            // Un-mirror the text - the Box itself is already flipped 180 degrees
            Text(
                text = card.content,
                color = Color.White,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        }
    }
}
