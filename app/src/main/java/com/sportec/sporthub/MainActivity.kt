package com.sportec.sporthub

import android.os.Bundle
import android.content.Intent
import com.sportec.sporthub.utils.EntradaNotificacion
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sportec.sporthub.navigation.SportHubApp
import com.sportec.sporthub.ui.theme.SportHubTheme
import com.sportec.sporthub.utils.Textos
import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.data.FotosRemotas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Textos.iniciar(this)
        FotosLocales.iniciar(this)
        FotosRemotas.iniciar(this)
        if (savedInstanceState == null) EntradaNotificacion.recibir(intent)
        enableEdgeToEdge()
        setContent {
            SportHubTheme {
                SportHubApp()
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        EntradaNotificacion.recibir(intent)
    }
}
