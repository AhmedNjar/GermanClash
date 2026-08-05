package com.example.germanclash.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.germanclash.presentation.theme.GameColors

@Composable
fun CategoryChip(
    emoji: String,
    label: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.28f) else GameColors.CardBackground,
        label = "categoryChipBackground"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(BorderStroke(1.5.dp, accentColor), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji)
        Text(text = "  $label", color = Color.White)
    }
}
