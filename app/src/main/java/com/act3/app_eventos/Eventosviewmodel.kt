package com.act3.app_eventos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// Se usa para leer lo que escribe el usuario y también para mostrar las fechas
val formatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")

// Largo máximo permitido para la descripción
const val MAX_DESCRIPCION = 100

enum class Prioridad(val nombre: String, val color: Color) {
    Alta("Alta", Color(0xFFD32F2F)),
    Media("Media", Color(0xFFFFB300)),
    Baja("Baja", Color(0xFF2E7D32))
}

enum class Categoria(val nombre: String, val icono: ImageVector) {
    Futbol("Fútbol", Icons.Filled.SportsSoccer),
    Voley("Voley", Icons.Filled.SportsVolleyball),
    Tenis("Tenis", Icons.Filled.SportsTennis),
    Basquet("Basquet", Icons.Filled.SportsBasketball),
    Esports("Esports", Icons.Filled.SportsEsports)
}

data class Evento(
    val id: Long, // sirve para identificar cada tarjeta al animar y al eliminar
    val titulo: String,
    val fechaHora: LocalDateTime,
    val categoria: Categoria,
    val lugar: String = "Por definir",
    val prioridad: Prioridad = Prioridad.Media,
    val descripcion: String = ""
)

@Singleton
class EventosRepository @Inject constructor() {

    // Lista de eventos, al cambiar (agregar o eliminar) la pantalla se actualiza sola
    val eventos = mutableStateListOf<Evento>()
    private var siguienteId = 1L

    init {
        // Datos de ejemplo para que la lista no inicie vacía
        val ahora = LocalDateTime.now()
        agregar("Fútbol Masculino", ahora.plusHours(2), Categoria.Futbol, "CDTAR", Prioridad.Alta, "Partido de la jornada 2, llegar 30 minutos antes.")
        agregar("Fútbol Femenil", ahora.plusDays(1), Categoria.Futbol, "CDTAR", Prioridad.Media, "Semifinal del torneo femenil.")
        agregar("Fútbol Mixto", ahora.plusDays(3), Categoria.Futbol, "Tecmilenio", Prioridad.Baja, "Partido amistoso entre carreras.")
        agregar("Fútbol Varonil Jornada 1", ahora.minusDays(2), Categoria.Futbol, "CDTAR", Prioridad.Media, "Partido ya jugado.")
        agregar("Voley Mixto", ahora.minusDays(1), Categoria.Voley, "Tecmilenio", Prioridad.Baja, "")
    }

    fun agregar(
        titulo: String,
        fechaHora: LocalDateTime,
        categoria: Categoria,
        lugar: String,
        prioridad: Prioridad,
        descripcion: String
    ) {
        eventos.add(Evento(siguienteId++, titulo, fechaHora, categoria, lugar, prioridad, descripcion))
    }

    fun eliminar(evento: Evento) {
        eventos.remove(evento)
    }
}

// VIEWMODEL: estado de las pantallas y validaciones

@HiltViewModel
class EventosViewModel @Inject constructor(
    private val repositorio: EventosRepository
) : ViewModel() {

    // Categoría que el usuario eligió en la primera pantalla
    var categoria by mutableStateOf(Categoria.Futbol)
        private set

    // Tema: null significa "usar el del sistema", true/false cuando el usuario lo cambia con el botón
    var temaOscuro by mutableStateOf<Boolean?>(null)
        private set

    // Estado del formulario
    var titulo by mutableStateOf("")
        private set
    var fecha by mutableStateOf("")
        private set
    var prioridad by mutableStateOf(Prioridad.Media)
        private set
    var descripcion by mutableStateOf("")
        private set
    var intentoGuardar by mutableStateOf(false)
        private set

    fun elegirCategoria(nueva: Categoria) {
        categoria = nueva
    }

    // Recibe el tema que se está viendo ahora y guarda el contrario
    fun alternarTema(actualOscuro: Boolean) {
        temaOscuro = !actualOscuro
    }

    // Eventos de la categoría elegida, ordenados por fecha: próximos (true) o finalizados (false)
    fun eventosDe(proximos: Boolean): List<Evento> {
        val ahora = LocalDateTime.now()
        return repositorio.eventos
            .filter { it.categoria == categoria && it.fechaHora.isAfter(ahora) == proximos }
            .sortedBy { it.fechaHora }
    }

    fun cambiarTitulo(nuevo: String) {
        titulo = nuevo
    }

    fun cambiarFecha(nueva: String) {
        fecha = nueva
    }

    fun cambiarPrioridad(nueva: Prioridad) {
        prioridad = nueva
    }

    fun cambiarDescripcion(nueva: String) {
        descripcion = nueva
    }

    fun limpiarFormulario() {
        titulo = ""
        fecha = ""
        prioridad = Prioridad.Media
        descripcion = ""
        intentoGuardar = false
    }

    fun eliminar(evento: Evento) {
        repositorio.eliminar(evento)
    }

    // Validaciones en tiempo real ----------
    val errorTitulo: String?
        get() = when {
            titulo.isEmpty() -> if (intentoGuardar) "El título no puede estar vacío" else null
            titulo.isBlank() -> "El título no puede estar vacío"
            titulo.trim().length < 3 -> "Escribe al menos 3 caracteres"
            else -> null
        }

    val errorFecha: String?
        get() {
            if (fecha.isEmpty()) return if (intentoGuardar) "La fecha es obligatoria" else null
            val fechaHora = fechaParseada()
                ?: // Mientras la fecha está incompleta no se marca error, solo al terminar de escribirla
                return if (fecha.length >= 16 || intentoGuardar) "Usa el formato dd/MM/yy - HH:mm" else null
            return if (fechaHora.isAfter(LocalDateTime.now())) null else "La fecha y hora deben ser futuras"
        }

    val errorDescripcion: String?
        get() = if (descripcion.length > MAX_DESCRIPCION) "Máximo $MAX_DESCRIPCION caracteres" else null

    val tituloValido: Boolean
        get() = titulo.isNotBlank() && errorTitulo == null

    val fechaValida: Boolean
        get() = fecha.isNotEmpty() && errorFecha == null

    private fun fechaParseada(): LocalDateTime? =
        runCatching { LocalDateTime.parse(fecha.trim(), formatoFecha) }.getOrNull()

    // Regresa true si se guardó, false si hay errores en el formulario
    fun guardar(): Boolean {
        intentoGuardar = true
        val fechaHora = fechaParseada()
        if (errorTitulo != null || errorFecha != null || errorDescripcion != null || fechaHora == null) {
            return false
        }

        repositorio.agregar(titulo.trim(), fechaHora, categoria, "Por definir", prioridad, descripcion.trim())
        limpiarFormulario()
        return true
    }
}