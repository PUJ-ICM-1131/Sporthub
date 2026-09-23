package com.sportec.sporthub.ui.screens.acceso

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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

    PantallaBase(titulo = Textos.obtener(R.string.texto_registro_de_establecimiento), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.texto_datos_de_la_persona_de_contacto), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.nombreContacto,
                onValueChange = viewModel::onNombreContactoCambiado,
                label = { Text(stringResource(R.string.texto_nombre_completo)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.email,
                onValueChange = viewModel::onEmailCambiado,
                label = { Text(stringResource(R.string.texto_correo_electronico)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.telefono,
                onValueChange = viewModel::onTelefonoCambiado,
                label = { Text(stringResource(R.string.texto_telefono)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = viewModel::onContrasenaCambiada,
                label = { Text(stringResource(R.string.texto_contrasena)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider()
            Text(stringResource(R.string.texto_datos_del_negocio), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = estado.nombreNegocio,
                onValueChange = viewModel::onNombreNegocioCambiado,
                label = { Text(stringResource(R.string.texto_nombre_del_negocio)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.nit,
                onValueChange = viewModel::onNitCambiado,
                label = { Text(stringResource(R.string.texto_nit)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.direccion,
                onValueChange = viewModel::onDireccionCambiada,
                label = { Text(stringResource(R.string.texto_direccion)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(onClick = viewModel::registrar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.texto_crear_cuenta))
            }
        }
    }
}
