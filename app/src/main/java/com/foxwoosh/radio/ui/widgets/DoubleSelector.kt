package com.foxwoosh.radio.ui.widgets

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxwoosh.radio.ui.singleCondition
import com.foxwoosh.radio.ui.theme.BlackOverlay_20
import com.foxwoosh.radio.ui.theme.WhiteOverlay_20

private val shape = RoundedCornerShape(14.dp)

@Composable
fun DoubleSelector(
    modifier: Modifier = Modifier,
    @IntRange(from = -1, to = 1) selectedIndex: Int,
    firstItemText: String,
    secondItemText: String,
    onSelectAction: (index: Int) -> Unit,
    background: Color = BlackOverlay_20,
    enabled: Boolean = true
) {
    Row(
        modifier
            .clip(shape)
            .background(background)
    ) {
        DoubleSelectorButton(
            text = firstItemText,
            selectedIndex == 0,
            onClick = { onSelectAction(0) },
            enabled = enabled
        )
        DoubleSelectorButton(
            text = secondItemText,
            selected = selectedIndex == 1,
            onClick = { onSelectAction(1) },
            enabled = enabled
        )
    }
}

// TODO: animate selection change
@Composable
fun DoubleSelectorButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick, enabled = enabled)
            .singleCondition(selected) { background(WhiteOverlay_20) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        fontSize = 14.sp
    )
}