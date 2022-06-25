package com.foxwoosh.radio.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow

val Insets = MutableStateFlow(WindowInsetsCompat.CONSUMED)

inline val WindowInsetsCompat.statusBar: Dp
    @Composable
    get() = with(LocalDensity.current) {
        getInsets(WindowInsetsCompat.Type.statusBars()).top.toDp()
    }

inline val WindowInsetsCompat.navigationBar: Dp
    @Composable
    get() = with(LocalDensity.current) {
        getInsets(WindowInsetsCompat.Type.navigationBars()).bottom.toDp()
    }