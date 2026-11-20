package com.sportec.sporthub.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

enum class OrigenEncuentro { CHAT, COMUNIDAD }
enum class EstadoEncuentro { ABIERTO, FINALIZADO }

data class PuntoReunion(val latitud: Double, val longitud: Double, val autorId: String)

data class Encuentro(
    val id: String,
    val origenTipo: OrigenEncuentro,
    /** Id del chat individual o de la comunidad (que es también el id de su chat). */
    val origenId: String,
    val creadorId: String,
    val estado: EstadoEncuentro,
    val punto: PuntoReunion?
)

data class PosicionCompartida(
    val uid: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float?,
    /** Hora del servidor de la última actualización (ms). */
    val actualizadaEn: Long
) {
    /** Reciente: se presenta como ubicación en vivo. */
    fun enVivo(ahora: Long = System.currentTimeMillis()) = ahora - actualizadaEn <= Encuentros.SEGUNDOS_EN_VIVO * 1000L
    /** Vigente: aún se muestra, marcada como desactualizada si ya no es [enVivo]. */
    fun vigente(ahora: Long = System.currentTimeMillis()) = ahora - actualizadaEn <= Encuentros.SEGUNDOS_VIGENCIA * 1000L
}

/**
 * Datos y permisos de la pantalla «Encontrarnos» (la interfaz y el mapa son de Persona C).
 *
 * Firestore:
 * - `encuentros/{e_<origenId>}`: un encuentro por chat o comunidad. Quién puede entrar se decide en
 *   las reglas a partir de los miembros actuales del chat o de la comunidad, así que dejar la
 *   comunidad revoca el acceso sin ninguna operación adicional.
 * - `encuentros/{id}/posiciones/{uid}`: la existencia del documento ES el consentimiento de esa persona.
 *   Solo su dueño lo escribe, con hora del servidor; borrarlo es «dejar de compartir».
 * - Campo `punto` del encuentro: el punto común, independiente de las posiciones personales.
 *
 * No se guarda historial: cada persona tiene como máximo un documento de posición.
 */
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
            punto = punto
        )
    }

    /** Abre (o reabre si estaba finalizado) el encuentro de un chat o comunidad y devuelve su id. */
    suspend fun abrir(origenTipo: OrigenEncuentro, origenId: String): String {
        val uid = Nube.uid() ?: throw IllegalStateException()
        val id = idDe(origenId)
        val ref = Nube.db.document(ruta(id))
        val actual = ref.get().await()
        if (!actual.exists()) {
            ref.set(mapOf(
                "origenTipo" to origenTipo.name, "origenId" to origenId, "creadorId" to uid,
                "estado" to EstadoEncuentro.ABIERTO.name, "punto" to null, "creadoEn" to Nube.ahora()
            )).await()
        } else if (actual.getString("estado") == EstadoEncuentro.FINALIZADO.name) {
            ref.update(mapOf("estado" to EstadoEncuentro.ABIERTO.name, "punto" to null)).await()
        }
        return id
    }

    fun observar(id: String): Flow<Encuentro?> = Nube.db.document(ruta(id)).snapshots()
        .map { encuentro(it) }
        .catch { Log.w(ETIQUETA, "Sin acceso al encuentro $id", it); emit(null) }

    /** Posiciones de quienes comparten ahora; incluye las desactualizadas para que la interfaz las marque. */
    fun observarPosiciones(id: String): Flow<List<PosicionCompartida>> =
        Nube.db.collection("${ruta(id)}/posiciones").snapshots()
            .map { s ->
                s.documents.mapNotNull { d ->
                    PosicionCompartida(
                        uid = d.id,
                        latitud = d.getDouble("lat") ?: return@mapNotNull null,
                        longitud = d.getDouble("lng") ?: return@mapNotNull null,
                        precisionMetros = d.getDouble("precision")?.toFloat(),
                        actualizadaEn = Mapeos.marcaTiempo(d, "actualizadoEn")
                    )
                }.filter { it.vigente() }
            }
            .catch { Log.w(ETIQUETA, "Sin acceso a las posiciones de $id", it); emit(emptyList()) }

    /** Activa o actualiza la posición propia. Llamar solo mientras el interruptor esté activado. */
    suspend fun publicarPosicion(id: String, latitud: Double, longitud: Double, precisionMetros: Float?) {
        val uid = Nube.uid() ?: return
        Nube.db.document("${ruta(id)}/posiciones/$uid").set(mapOf(
            "lat" to latitud, "lng" to longitud, "precision" to precisionMetros?.toDouble(),
            "actualizadoEn" to Nube.ahora()
        )).await()
    }

    /** Apagar el interruptor o salir de la pantalla: retira la posición propia; el punto común permanece. */
    suspend fun dejarDeCompartir(id: String) {
        val uid = Nube.uid() ?: return
        Nube.db.document("${ruta(id)}/posiciones/$uid").delete().await()
    }

    /** «Nos encontramos aquí»: reemplaza el punto común del encuentro. */
    suspend fun proponerPunto(id: String, latitud: Double, longitud: Double) {
        val uid = Nube.uid() ?: return
        Nube.db.document(ruta(id)).update(mapOf(
            "punto" to mapOf("lat" to latitud, "lng" to longitud, "autorId" to uid)
        )).await()
    }

    /** Cierra el encuentro para todos (lo permiten quien lo creó o el administrador de la comunidad). */
    suspend fun finalizar(id: String) {
        Nube.db.document(ruta(id)).update(mapOf("estado" to EstadoEncuentro.FINALIZADO.name)).await()
    }
}
