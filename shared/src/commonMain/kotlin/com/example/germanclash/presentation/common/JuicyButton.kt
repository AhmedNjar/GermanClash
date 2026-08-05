package com.example.germanclash.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.germanclash.presentation.theme.GameColors

enum class AnswerState { IDLE, SELECTED, CORRECT, WRONG }

/**
 * Every mini-game's tappable option (a DER_DIE_DAS article, an IMAGE_GUESS
 * choice, an ODD_ONE_OUT word) is one of these - shrinks on press, and
 * animates to green/red once `state` reflects a round result.
 */
@Composable
fun JuicyButton(
    text: String,
    state: AnswerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            AnswerState.IDLE -> GameColors.OptionDefault
            AnswerState.SELECTED -> GameColors.OptionSelected
            AnswerState.CORRECT -> GameColors.CorrectGreen
            AnswerState.WRONG -> GameColors.WrongRed
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = state == AnswerState.IDLE || state == AnswerState.SELECTED,
                onClick = onClick
            )
            .padding(vertical = 18.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
