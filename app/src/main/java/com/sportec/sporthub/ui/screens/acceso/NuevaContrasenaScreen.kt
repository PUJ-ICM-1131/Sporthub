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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun NuevaContrasenaScreen(email: String, onBack: () -> Unit, onListo: () -> Unit) {
    var contrasena by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_nueva_contrasena), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; error = null },
                label = { Text(stringResource(R.string.texto_nueva_contrasena)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    try {
                        Cuentas.restablecerContrasena(email, contrasena)
                        onListo()
                    } catch (e: Exception) {
                        error = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_actualizar_la_contrasena)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.texto_guardar_y_volver_a_iniciar_sesion))
            }
        }
    }
}
