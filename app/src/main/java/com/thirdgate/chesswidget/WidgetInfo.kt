package com.thirdgate.chesswidget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetInfo(
    val games: Int,
    val wins: Int,
    val losses: Int
)


