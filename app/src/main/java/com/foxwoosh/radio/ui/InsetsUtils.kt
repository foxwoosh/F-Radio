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

inline val WindowInsetsCompat.systemBottom: Dp
    @Composable
    get() = with(LocalDensity.current) {
        val ime = getInsets(WindowInsetsCompat.Type.ime()).bottom

        if (ime == 0) {
            getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        } else {
            ime
        }.toDp()
    }