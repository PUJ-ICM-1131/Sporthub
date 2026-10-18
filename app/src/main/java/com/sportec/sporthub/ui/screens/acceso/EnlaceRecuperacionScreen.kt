package com.sportec.sporthub.ui.screens.acceso

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun EnlaceRecuperacionScreen(email: String, onBack: () -> Unit, onVolverAIniciarSesion: () -> Unit) {
    PantallaBase(titulo = Textos.obtener(R.string.texto_enlace_de_recuperacion), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.texto_enviamos_un_enlace_de_recuperacion_a),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.texto_esta_es_una_demostracion_usa_el_boton_para_simular),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onVolverAIniciarSesion, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.texto_abrir_enlace_de_recuperacion))
            }
        }
    }
}
