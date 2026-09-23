package com.sportec.sporthub.ui.screens.acceso

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import com.sportec.sporthub.utils.Validaciones
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun RecuperarScreen(onBack: () -> Unit, onEnlaceEnviado: (String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }

    PantallaBase(titulo = Textos.obtener(R.string.texto_recuperar_acceso), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.texto_te_enviaremos_un_enlace_de_recuperacion_a_tu_correo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.texto_correo_electronico)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onEnlaceEnviado(email.trim()) },
                enabled = Validaciones.correo(email),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.texto_enviar_enlace))
            }
        }
    }
}
