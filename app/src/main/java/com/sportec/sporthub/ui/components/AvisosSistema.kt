package com.sportec.sporthub.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sportec.sporthub.MainActivity
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Notificaciones
import com.sportec.sporthub.utils.EntradaNotificacion

@Composable
fun AvisosSistema(usuarioId: String?) {
    val context = LocalContext.current
    val nombreCanal = stringResource(R.string.avisos_canal)
    val manager = remember { context.getSystemService(NotificationManager::class.java) }
    val avisos by Notificaciones.notificaciones.collectAsState()
    val versionPermisos by CambiosPermisos.version.collectAsState()
    val preferencias = remember { context.getSharedPreferences("avisos_mostrados", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(usuarioId, avisos, versionPermisos, nombreCanal) {
        if (usuarioId == null) {
            manager.cancelAll()
            return@LaunchedEffect
        }
        manager.createNotificationChannel(NotificationChannel("actividad", nombreCanal, NotificationManager.IMPORTANCE_DEFAULT))
        val vigentes = avisos.filter { it.usuarioId == usuarioId && !it.leida }.map { it.id }.toSet()
        manager.activeNotifications.filter { it.tag !in vigentes }.forEach { manager.cancel(it.tag, it.id) }
        val permitidas = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!permitidas || !manager.areNotificationsEnabled()) return@LaunchedEffect
        avisos.filter { it.usuarioId == usuarioId && !it.leida && !preferencias.getBoolean("$usuarioId:${it.id}", false) }.forEach { aviso ->
            val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(EntradaNotificacion.USUARIO, usuarioId)
            intent.putExtra(EntradaNotificacion.AVISO, aviso.id)
            intent.data = android.net.Uri.Builder().scheme("sporthub").authority("aviso")
                .appendPath(usuarioId).appendPath(aviso.id).build()
            val abrir = PendingIntent.getActivity(context, usuarioId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notificacion = NotificationCompat.Builder(context, "actividad")
                .setSmallIcon(R.drawable.ic_aviso)
                .setContentTitle(aviso.titulo).setContentText(aviso.texto)
                .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.texto))
                .setContentIntent(abrir).setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
            try {
                manager.notify(aviso.id, 0, notificacion)
                preferencias.edit().putBoolean("$usuarioId:${aviso.id}", true).apply()
            } catch (_: SecurityException) { }
        }
    }
}
