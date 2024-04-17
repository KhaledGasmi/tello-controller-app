package com.tello.controltello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tello.controltello.livecontrol.presentation.LiveControlScreen
import com.tello.controltello.ui.theme.ControlTelloTheme

class MainActivity : ComponentActivity() {
    private val liveControlScreen = LiveControlScreen()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlTelloTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    liveControlScreen.LiveControlScreen()
                }
            }
        }
    }
}
