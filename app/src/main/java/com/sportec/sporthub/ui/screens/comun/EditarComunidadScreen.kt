package com.sportec.sporthub.ui.screens.comun

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun EditarComunidadScreen(
    comunidadId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onGuardada: () -> Unit
) {
    val comunidades by Comunidades.comunidades.collectAsState()
    val comunidad = comunidades.firstOrNull { it.id == comunidadId }
    var nombre by rememberSaveable { mutableStateOf(comunidad?.nombre.orEmpty()) }
    var descripcion by rememberSaveable { mutableStateOf(comunidad?.descripcion.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = Textos.obtener(R.string.comunidad_editar_titulo), onBack = onBack) { padding ->
        if (comunidad == null) {
            EstadoVacio(
                mensaje = stringResource(R.string.comunidad_no_encontrada),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.comunidad_campo_nombre)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text(stringResource(R.string.comunidad_campo_descripcion)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    try {
                        Comunidades.editar(comunidadId, nombre, descripcion, usuarioId)
                        onGuardada()
                    } catch (e: Exception) {
                        error = e.message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.comunidad_guardar))
            }
        }
    }
}
