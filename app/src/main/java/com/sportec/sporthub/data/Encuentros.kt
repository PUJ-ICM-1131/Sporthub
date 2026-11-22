package com.sportec.sporthub.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import com.google.firebase.firestore.Source
import com.sportec.sporthub.domain.AccesoEncuentros
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.tasks.await

enum class OrigenEncuentro { CHAT, COMUNIDAD }
enum class EstadoEncuentro { ABIERTO, FINALIZADO }

data class PuntoReunion(val latitud: Double, val longitud: Double, val autorId: String)

data class Encuentro(
    val id: String,
    val origenTipo: OrigenEncuentro,

    val origenId: String,
    val creadorId: String,
    val estado: EstadoEncuentro,
    val punto: PuntoReunion?,

    val sesion: String = ""
)

data class PosicionCompartida(
    val uid: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float?,

    val actualizadaEn: Long
) {

    fun enVivo(ahora: Long = System.currentTimeMillis()) = ahora - actualizadaEn in 0..Encuentros.SEGUNDOS_EN_VIVO * 1000L

    fun vigente(ahora: Long = System.currentTimeMillis()) = ahora - actualizadaEn in 0..Encuentros.SEGUNDOS_VIGENCIA * 1000L
}

object Encuentros {
    private const val ETIQUETA = "Encuentros"
    const val SEGUNDOS_EN_VIVO = 60
    const val SEGUNDOS_VIGENCIA = 300

    private fun ruta(id: String) = "encuentros/$id"
    fun idDe(origenId: String) = "e_$origenId"

    private fun encuentro(d: DocumentSnapshot): Encuentro? {
        if (!d.exists()) return null
        val punto = (d.get("punto") as? Map<*, *>)?.let {
            val lat = (it["lat"] as? Number)?.toDouble() ?: return@let null
            val lng = (it["lng"] as? Number)?.toDouble() ?: return@let null
            PuntoReunion(lat, lng, it["autorId"] as? String ?: "")
        }
        return Encuentro(
            id = d.id,
            origenTipo = OrigenEncuentro.values().firstOrNull { it.name == d.getString("origenTipo") } ?: return null,
            origenId = d.getString("origenId") ?: return null,
            creadorId = d.getString("creadorId").orEmpty(),
            estado = EstadoEncuentro.values().firstOrNull { it.name == d.getString("estado") } ?: return null,
            punto = punto,
            sesion = d.getString("sesion").orEmpty()
        )
    }

    suspend fun abrir(origenTipo: OrigenEncuentro, origenId: String): String {
        val uid = Nube.uid() ?: throw IllegalStateException()

        check(AccesoEncuentros.puedeEntrar(origenId, uid))
        val id = idDe(origenId)
        val ref = Nube.db.document(ruta(id))
        val nuevaSesion = UUID.randomUUID().toString()
        Nube.db.runTransaction { tx ->
            val actual = tx.get(ref)
            if (!actual.exists()) {
                tx.set(ref, mapOf(
                    "origenTipo" to origenTipo.name, "origenId" to origenId, "creadorId" to uid,
                    "estado" to EstadoEncuentro.ABIERTO.name, "punto" to null,
                    "sesion" to nuevaSesion, "creadoEn" to Nube.ahora()
                ))
            } else if (actual.getString("estado") == EstadoEncuentro.FINALIZADO.name || actual.getString("sesion").isNullOrBlank()) {
                tx.update(ref, mapOf("estado" to EstadoEncuentro.ABIERTO.name, "punto" to null, "sesion" to nuevaSesion))
            }
        }.await()
        return id
    }

    fun observar(id: String): Flow<Encuentro?> = Nube.db.document(ruta(id)).snapshots()
        .map { encuentro(it) }
        .catch { Log.w(ETIQUETA, "Sin acceso al encuentro $id", it); emit(null) }

    private fun posicionRuta(id: String, sesion: String, uid: String) =
        "${ruta(id)}/sesiones/$sesion/posiciones/$uid"

    fun observarPosiciones(id: String, sesion: String, participantes: List<String>): Flow<List<PosicionCompartida>> {
        if (sesion.isBlank() || participantes.isEmpty()) return flowOf(emptyList())
        val lecturas = participantes.distinct().map { uid ->
            Nube.db.document(posicionRuta(id, sesion, uid)).snapshots()
                .map { d ->
                    val lat = d.getDouble("lat")
                    val lng = d.getDouble("lng")
                    val instante = d.getTimestamp("actualizadoEn")?.toDate()?.time

                    if (lat == null || lng == null || instante == null) null
                    else PosicionCompartida(uid, lat, lng, d.getDouble("precision")?.toFloat(), instante).takeIf { it.vigente() }
                }
                .retryWhen { causa, _ ->
                    if (causa is CancellationException) throw causa
                    emit(null)
                    delay(5_000)
                    true
                }
        }
        return combine(lecturas) { valores -> valores.filterNotNull() }
    }

    suspend fun publicarPosicion(id: String, sesion: String, latitud: Double, longitud: Double, precisionMetros: Float?) {
        val uid = Nube.uid() ?: return
        require(sesion.isNotBlank())
        Nube.db.document(posicionRuta(id, sesion, uid)).set(mapOf(
            "lat" to latitud, "lng" to longitud, "precision" to precisionMetros?.toDouble(),
            "actualizadoEn" to Nube.ahora()
        )).await()
    }

    suspend fun dejarDeCompartir(id: String, sesion: String) {
        val uid = Nube.uid() ?: return
        if (sesion.isNotBlank()) Nube.db.document(posicionRuta(id, sesion, uid)).delete().await()
    }

    suspend fun proponerPunto(id: String, latitud: Double, longitud: Double) {
        val uid = Nube.uid() ?: return
        Nube.db.document(ruta(id)).update(mapOf(
            "punto" to mapOf("lat" to latitud, "lng" to longitud, "autorId" to uid)
        )).await()
    }

    suspend fun finalizar(id: String) {
        Nube.db.document(ruta(id)).update(mapOf("estado" to EstadoEncuentro.FINALIZADO.name)).await()
    }
}
