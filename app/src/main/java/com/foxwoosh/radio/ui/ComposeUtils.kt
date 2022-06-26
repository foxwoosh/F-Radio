package com.foxwoosh.radio.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.borderlessClickable(
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
): Modifier = composed {
    val h = LocalHapticFeedback.current

    combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(
            bounded = false,
            radius = radius,
            color = color
        ),
        onClick = onClick,
        onLongClick = {
            onLongClick?.let {
                it.invoke()
                h.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        },
        onDoubleClick = onDoubleClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.longClickable(
    onLongClick: () -> Unit
): Modifier = composed {
    val h = LocalHapticFeedback.current

    combinedClickable(
        onClick = {},
        onLongClick = {
            h.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        }
    )
}

fun Modifier.singleCondition(
    condition: Boolean,
    function: Modifier.() -> Modifier
) = if (condition) {
    function()
} else {
    this
}

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetScaffoldState.currentOffset: Float
    get() {
        val progress = bottomSheetState.progress
        val fraction = progress.fraction
        val from = progress.from
        val to = progress.to

        return when {
            from == BottomSheetValue.Collapsed && to == BottomSheetValue.Collapsed -> 0f
            from == BottomSheetValue.Expanded && to == BottomSheetValue.Expanded -> 1f
            from == BottomSheetValue.Collapsed && to == BottomSheetValue.Expanded -> fraction
            else -> 1f - fraction
        }
    }

@Composable
fun <T> Flow<T>.collectAsEffect(
    context: CoroutineContext = EmptyCoroutineContext,
    block: (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        onEach(block).flowOn(context).launchIn(this)
    }
}