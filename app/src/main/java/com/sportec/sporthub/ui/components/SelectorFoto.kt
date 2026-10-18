package com.sportec.sporthub.ui.components

import android.net.Uri
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sportec.sporthub.utils.FotosLocales
import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun SelectorFoto(
    fotoUri: String?,
    onFotoSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 160.dp,
    forma: Shape = RoundedCornerShape(17.dp),
    onProcesando: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var procesando by remember { mutableStateOf(false) }
    var mostrarOpciones by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<Int?>(null) }
    var capturaUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturaNombre by rememberSaveable { mutableStateOf<String?>(null) }

    fun limpiarCaptura() {
        capturaNombre?.let { nombre ->
            val carpeta = File(context.cacheDir, "capturas")
            val archivo = File(carpeta, nombre)
            if (archivo.parentFile?.canonicalPath == carpeta.canonicalPath) {
                archivo.delete()
            }
        }
        capturaUri = null
        capturaNombre = null
    }

    fun importar(uri: Uri, esCaptura: Boolean) {
        procesando = true
        onProcesando(true)
        scope.launch {
            try {
                val local = withContext(Dispatchers.IO) { FotosLocales.importar(uri) }
                onFotoSeleccionada(local)
            } catch (_: IOException) {
                error = R.string.selector_foto_error_lectura
            } catch (_: SecurityException) {
                error = R.string.selector_foto_error_lectura
            } finally {
                if (esCaptura) limpiarCaptura()
                procesando = false
                onProcesando(false)
            }
        }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        val uri = capturaUri
        if (guardada && uri != null) importar(Uri.parse(uri), true)
        else limpiarCaptura()
    }
    fun abrirCamara() {
        try {
            val carpeta = File(context.cacheDir, "capturas")
            if (!carpeta.exists() && !carpeta.mkdirs()) throw IOException()
            val archivo = File.createTempFile("foto_", ".jpg", carpeta)
            capturaNombre = archivo.name
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            capturaUri = uri.toString()
            camara.launch(uri)
        } catch (_: ActivityNotFoundException) {
            limpiarCaptura()
            error = R.string.selector_foto_sin_camara
        } catch (_: IOException) {
            limpiarCaptura()
            error = R.string.selector_foto_error_captura
        } catch (_: SecurityException) {
            limpiarCaptura()
            error = R.string.selector_foto_error_captura
        } catch (_: IllegalArgumentException) {
            limpiarCaptura()
            error = R.string.selector_foto_error_captura
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        preferencias.edit().putBoolean("camara_denegada", !concedido).apply()
        if (concedido) abrirCamara() else error = R.string.camara_permiso_denegado
    }
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) importar(uri, false)
    }
    if (mostrarOpciones) {
        AlertDialog(
            onDismissRequest = { mostrarOpciones = false },
            title = { Text(stringResource(R.string.selector_foto_titulo)) },
            text = {
                Column {
                    TextButton(onClick = {
                        mostrarOpciones = false
                        val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        when {
                            permitido -> abrirCamara()
                            preferencias.getBoolean("camara_denegada", false) && activity?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true -> error = R.string.camara_permiso_denegado
                            else -> permisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    }) { Text(stringResource(R.string.selector_foto_camara)) }
                    TextButton(onClick = {
                        mostrarOpciones = false
                        try {
                            selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (_: ActivityNotFoundException) {
                            error = R.string.selector_foto_sin_galeria
                        } catch (_: SecurityException) {
                            error = R.string.selector_foto_error_lectura
                        }
                    }) { Text(stringResource(R.string.selector_foto_galeria)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarOpciones = false }) {
                    Text(stringResource(R.string.selector_foto_cancelar))
                }
            }
        )
    }
    error?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text(stringResource(R.string.selector_foto_error_titulo)) },
            dismissButton = {
                if (mensaje == R.string.camara_permiso_denegado) TextButton(onClick = {
                    error = null
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                }) { Text(stringResource(R.string.camara_abrir_ajustes)) }
            },
            text = { Text(stringResource(mensaje)) },
            confirmButton = {
                TextButton(onClick = { error = null }) {
                    Text(stringResource(R.string.selector_foto_aceptar))
                }
            }
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = capturaUri == null && !procesando) { mostrarOpciones = true }
    ) {
        if (fotoUri != null) {
            AsyncImage(
                model = fotoUri,
                contentDescription = Textos.obtener(R.string.texto_foto),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (procesando) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = Textos.obtener(R.string.texto_cambiar_foto),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
