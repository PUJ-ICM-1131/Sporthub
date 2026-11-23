package com.sportec.sporthub.data

import android.util.Log
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.Rol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

object Semilla {
    private const val ETIQUETA = "Semilla"

    fun cargarSiHaceFalta(scope: CoroutineScope, cuenta: Cuenta) {
        if (cuenta.rol != Rol.ADMINISTRADOR) return
        scope.launch {
            try {
                val existentes = Nube.db.collection("negocios").limit(1).get().await()
                if (!existentes.isEmpty) return@launch
                val lote = Nube.db.batch()
                DatosMock.negocios.forEach {
                    lote.set(Nube.db.collection("negocios").document(it.id), Mapeos.mapa(it))
                }
                DatosMock.actividades.forEach {
                    lote.set(Nube.db.collection("actividades").document(it.id), Mapeos.mapa(it, emptyList()))
                }
                lote.commit().await()
                Log.i(ETIQUETA, "Catálogo inicial cargado en Firestore")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(ETIQUETA, "No se pudo cargar el catálogo inicial", e)
            }
        }
    }
}
