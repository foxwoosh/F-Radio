package com.foxwoosh.radio.domain.models

enum class LyricsReportState {
    SUBMITTED, DECLINED, SOLVED;

    companion object {
        fun get(value: String?) = values().find { it.name == value }
    }
}