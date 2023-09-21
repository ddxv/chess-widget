package com.thirdgate.chesswidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize

class MyWidget : GlanceAppWidget() {

    override val stateDefinition = GlanceButtonWidgetStateDefinition()
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.i("MyWidget", "provideGlance started")
        provideContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        Log.i(
            "MyWidget",
            "Content: start"
        )
        val widgetInfo = currentState<WidgetInfo>()
        val numGames = widgetInfo.games
        val numWins = widgetInfo.wins
        val numLosses = widgetInfo.losses
        val numDraws = numGames - numWins - numLosses
        val context = LocalContext.current
        val glanceId = LocalGlanceId.current

        Log.i("MyWidget", "Content: numGames=$numGames: check numWins=$numWins")

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .cornerRadius(8.dp)
            ) {
                            Log.i("MyWidget", "Content: got imageProvider")
                            //ChessGameGlance()
                    }

            }
        }
    }



class ChessWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyWidget()
}
