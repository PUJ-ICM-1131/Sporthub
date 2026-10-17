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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Textos.iniciar(this)
        com.sportec.sporthub.utils.FotosLocales.iniciar(this)
        if (savedInstanceState == null) EntradaNotificacion.destinatario.value = intent.getStringExtra(EntradaNotificacion.USUARIO)
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
        EntradaNotificacion.destinatario.value = intent.getStringExtra(EntradaNotificacion.USUARIO)
    }
}
