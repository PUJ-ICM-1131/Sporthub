package com.sportec.sporthub.ui.screens.acceso

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onRecuperar: () -> Unit,
    onAutenticado: (Cuenta) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.cuentaAutenticada) {
        val cuenta = estado.cuentaAutenticada
        if (cuenta != null) {
            viewModel.autenticacionAtendida()
            onAutenticado(cuenta)
        }
    }

    PantallaBase(titulo = "Iniciar sesión", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido de nuevo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Accede con tu cuenta de deportista, establecimiento o administrador.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = estado.usuario,
                onValueChange = viewModel::onUsuarioCambiado,
                label = { Text("Correo o usuario") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = viewModel::onContrasenaCambiada,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.iniciarSesion() }),
                trailingIcon = {
                    TextButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                        Text(if (mostrarContrasena) "Ocultar" else "Mostrar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            val mensajeError = estado.error
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = viewModel::iniciarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Ingresar")
            }
            TextButton(
                onClick = onRecuperar,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }
            HorizontalDivider()
            Text(
                text = "Cuentas de demostración",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Toca una cuenta para llenar el formulario. Contraseña: ${DatosMock.CONTRASENA_DEMO}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                DatosMock.cuentas.forEach { cuenta ->
                    ListItem(
                        headlineContent = { Text(cuenta.nombre) },
                        supportingContent = { Text("${cuenta.id} · ${cuenta.rol.etiqueta}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { viewModel.usarCuentaDemo(cuenta) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Simular fallo del servicio",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = estado.simularError,
                    onCheckedChange = viewModel::onSimularErrorCambiado
                )
            }
        }
    }
}
