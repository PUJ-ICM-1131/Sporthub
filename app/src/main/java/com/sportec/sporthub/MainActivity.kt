package com.sportec.sporthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sportec.sporthub.navigation.SportHubApp
import com.sportec.sporthub.ui.theme.SportHubTheme
import com.sportec.sporthub.utils.Textos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Textos.iniciar(this)
        enableEdgeToEdge()
        setContent {
            SportHubTheme {
                SportHubApp()
            }
        }
    }
}
