package com.sportec.sporthub.ui.screens.comun

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.SelectorFoto
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun EditarPerfilScreen(
    cuenta: Cuenta?,
    onBack: () -> Unit,
    onGuardado: (Cuenta) -> Unit,
    viewModel: EditarPerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var procesandoFoto by remember { mutableStateOf(false) }

    LaunchedEffect(cuenta?.id) { cuenta?.let(viewModel::cargar) }
    LaunchedEffect(estado.guardada) {
        estado.guardada?.let {
            onGuardado(it)
            onBack()
        }
    }

    PantallaBase(
        titulo = if (estado.tieneNegocio) Textos.obtener(R.string.perfil_editar_cuenta) else Textos.obtener(R.string.texto_editar_perfil),
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estado.tieneNegocio) {
                Text(
                    text = stringResource(R.string.perfil_persona_contacto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            SelectorFoto(
                fotoUri = estado.fotoUri,
                onFotoSeleccionada = viewModel::onFotoCambiada,
                onProcesando = { procesandoFoto = it },
                forma = CircleShape,
                alto = 120.dp,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambiado,
                label = { Text(stringResource(R.string.texto_nombre)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.telefono,
                onValueChange = viewModel::onTelefonoCambiado,
                label = { Text(stringResource(R.string.texto_telefono)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.tieneNegocio) {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.perfil_datos_negocio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Aviso(texto = stringResource(R.string.negocio_aviso_sensible), tipo = TipoAviso.ADVERTENCIA)
                OutlinedTextField(
                    value = estado.nombreNegocio,
                    onValueChange = viewModel::onNombreNegocioCambiado,
                    label = { Text(stringResource(R.string.negocio_nombre)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.nit,
                    onValueChange = viewModel::onNitCambiado,
                    label = { Text(stringResource(R.string.perfil_nit)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.descripcion,
                    onValueChange = viewModel::onDescripcionCambiada,
                    label = { Text(stringResource(R.string.texto_descripcion)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.contacto,
                    onValueChange = viewModel::onContactoCambiado,
                    label = { Text(stringResource(R.string.texto_contacto)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.direccion,
                    onValueChange = viewModel::onDireccionCambiada,
                    label = { Text(stringResource(R.string.perfil_direccion)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = { cuenta?.let(viewModel::guardar) },
                enabled = !procesandoFoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.texto_guardar_cambios))
            }
        }
    }
}
