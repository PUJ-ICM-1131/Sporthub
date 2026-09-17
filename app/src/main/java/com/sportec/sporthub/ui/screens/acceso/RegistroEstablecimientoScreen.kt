package com.sportec.sporthub.ui.screens.acceso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun RegistroEstablecimientoScreen(
    onBack: () -> Unit,
    onRegistrado: () -> Unit,
    viewModel: RegistroEstablecimientoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.registrado) { if (estado.registrado) onRegistrado() }

    PantallaBase(titulo = "Registro de establecimiento", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Datos de la persona de contacto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.nombreContacto,
                onValueChange = viewModel::onNombreContactoCambiado,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.email,
                onValueChange = viewModel::onEmailCambiado,
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.telefono,
                onValueChange = viewModel::onTelefonoCambiado,
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = viewModel::onContrasenaCambiada,
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider()
            Text("Datos del negocio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.nombreNegocio,
                onValueChange = viewModel::onNombreNegocioCambiado,
                label = { Text("Nombre del negocio") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.nit,
                onValueChange = viewModel::onNitCambiado,
                label = { Text("NIT") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.direccion,
                onValueChange = viewModel::onDireccionCambiada,
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(onClick = viewModel::registrar, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}
