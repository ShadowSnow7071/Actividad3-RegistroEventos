package com.act3.app_eventos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.act3.app_eventos.ui.theme.App_eventosTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: EventosViewModel = viewModel()
            // Si el usuario todavía no tocó el botón, se usa el tema del sistema
            App_eventosTheme(darkTheme = vm.temaOscuro ?: isSystemInDarkTheme()) {
                AppEventos(vm)
            }
        }
    }
}

// ---------- Navegación entre las 3 pantallas ----------

@Composable
fun AppEventos(vm: EventosViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "categorias") {
        composable("categorias") {
            PantallaCategorias(
                vm = vm,
                onElegir = {
                    vm.elegirCategoria(it)
                    nav.navigate("eventos")
                }
            )
        }
        composable("eventos") {
            PantallaEventos(
                vm = vm,
                onVolver = { nav.popBackStack() },
                onAgregar = {
                    vm.limpiarFormulario()
                    nav.navigate("nuevo")
                }
            )
        }
        composable("nuevo") {
            PantallaNuevoEvento(vm = vm, onVolver = { nav.popBackStack() })
        }
    }
}

// ---------- Estructura que comparten todas las pantallas ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pantalla(
    titulo: String,
    vm: EventosViewModel,
    onVolver: (() -> Unit)? = null,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val oscuro = vm.temaOscuro ?: isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        // Si hay a dónde volver, el título también funciona como botón (como en Figma)
                        modifier = if (onVolver != null) Modifier.clickable(onClick = onVolver) else Modifier
                    )
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Botón pequeño en la esquina inferior derecha: luna en modo claro, sol en modo oscuro
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = { vm.alternarTema(oscuro) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = if (oscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Cambiar tema"
                )
            }
        },
        content = contenido
    )
}

// ---------- Pantalla 1: Categorías ----------

@Composable
fun PantallaCategorias(vm: EventosViewModel, onElegir: (Categoria) -> Unit) {
    Pantalla(titulo = "Interhalcones 2027", vm = vm) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Categorias",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                // El espacio de abajo evita que el botón de tema tape las tarjetas
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Categoria.values().toList()) { categoria ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onElegir(categoria) }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = categoria.icono,
                                contentDescription = categoria.nombre,
                                tint = MaterialTheme.colorScheme.tertiary, // color de acento
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(categoria.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Pantalla 2: Eventos ----------

@Composable
fun PantallaEventos(vm: EventosViewModel, onVolver: () -> Unit, onAgregar: () -> Unit) {
    var pestana by remember { mutableIntStateOf(0) } // 0 = Próximos, 1 = Finalizados

    val ahora = LocalDateTime.now()
    val lista = vm.eventos
        .filter { it.categoria == vm.categoria && it.fechaHora.isAfter(ahora) == (pestana == 0) }
        .sortedBy { it.fechaHora }

    Pantalla(titulo = "Volver", vm = vm, onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp)
        ) {
            Text("Interhalcones 2027", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Eventos de ${vm.categoria.nombre}:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            // Colores de las pestañas: texto claro en modo oscuro y oscuro en modo claro (contraste legible)
            val colorActiva = MaterialTheme.colorScheme.onSurface
            val colorInactiva = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

            TabRow(selectedTabIndex = pestana) {
                Tab(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    selectedContentColor = colorActiva,
                    unselectedContentColor = colorInactiva,
                    text = { Text("Próximos") }
                )
                Tab(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    selectedContentColor = colorActiva,
                    unselectedContentColor = colorInactiva,
                    text = { Text("Finalizados") }
                )
            }

            if (lista.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay eventos en esta sección")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // key = id permite que cada tarjeta se anime al entrar, salir o moverse
                    items(lista, key = { it.id }) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            onEliminar = { vm.eliminar(evento) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            Button(onClick = onAgregar, modifier = Modifier.fillMaxWidth()) {
                Text("+ AGREGAR EVENTO")
            }
        }
    }
}

// Tarjeta que se desliza hacia la izquierda para eliminar y se toca para expandir o contraer
@Composable
fun TarjetaEvento(evento: Evento, onEliminar: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val colorSecundario = MaterialTheme.colorScheme.onSurfaceVariant
    var expandida by rememberSaveable { mutableStateOf(false) }

    // Estado del gesto de deslizar: al llegar al final se elimina el evento con vibración
    val estadoSwipe = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEliminar()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = estadoSwipe,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Fondo rojo con bote de basura que se descubre al deslizar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD32F2F))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar evento", tint = Color.White)
            }
        }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize() // animación suave al expandir y contraer
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expandida = !expandida
                    }
                    .padding(16.dp)
            ) {
                // ConstraintLayout: cada elemento se acomoda respecto a los otros
                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                    val (refIcono, refTitulo, refFecha, refLugar, refFlecha) = createRefs()

                    Icon(
                        imageVector = evento.categoria.icono,
                        contentDescription = evento.categoria.nombre,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(44.dp)
                            .constrainAs(refIcono) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                    )
                    Icon(
                        imageVector = if (expandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expandida) "Contraer" else "Expandir",
                        modifier = Modifier.constrainAs(refFlecha) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                    )
                    Text(
                        text = evento.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.constrainAs(refTitulo) {
                            start.linkTo(refIcono.end, margin = 16.dp)
                            end.linkTo(refFlecha.start, margin = 8.dp)
                            top.linkTo(parent.top)
                            width = Dimension.fillToConstraints
                        }
                    )
                    Text(
                        text = evento.fechaHora.format(formatoFecha),
                        color = colorSecundario,
                        modifier = Modifier.constrainAs(refFecha) {
                            start.linkTo(refTitulo.start)
                            top.linkTo(refTitulo.bottom, margin = 2.dp)
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.constrainAs(refLugar) {
                            start.linkTo(refTitulo.start)
                            top.linkTo(refFecha.bottom, margin = 2.dp)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = colorSecundario,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(evento.lugar, color = colorSecundario)
                    }
                }

                // Parte que aparece al expandir la tarjeta
                if (expandida) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(evento.prioridad.color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Prioridad ${evento.prioridad.nombre}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = evento.descripcion.ifBlank { "Sin descripción" },
                        color = colorSecundario
                    )
                }
            }
        }
    }
}

// ---------- Pantalla 3: Registrar nuevo evento ----------

@Composable
fun PantallaNuevoEvento(vm: EventosViewModel, onVolver: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Pantalla(titulo = "Volver", vm = vm, onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp)
        ) {
            Text(
                text = "Registrar nuevo Evento",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Los campos van en una columna con scroll para que el teclado no los tape
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Campo(
                    etiqueta = "Titulo del Evento: *",
                    valor = vm.titulo,
                    onCambio = vm::cambiarTitulo,
                    error = vm.errorTitulo,
                    valido = vm.tituloValido
                )
                Campo(
                    etiqueta = "Fecha y Hora: * (dd/MM/yy - HH:mm)",
                    valor = vm.fecha,
                    onCambio = vm::cambiarFecha,
                    error = vm.errorFecha,
                    valido = vm.fechaValida
                )

                Column {
                    Text("Prioridad:", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Prioridad.values().forEach { p ->
                            FilterChip(
                                selected = vm.prioridad == p,
                                onClick = { vm.cambiarPrioridad(p) },
                                label = { Text(p.nombre) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                Column {
                    Campo(
                        etiqueta = "Descripcion (opcional):",
                        valor = vm.descripcion,
                        onCambio = vm::cambiarDescripcion,
                        error = vm.errorDescripcion,
                        unaLinea = false
                    )
                    Text(
                        text = "${vm.descripcion.length}/$MAX_DESCRIPCION",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Button(
                onClick = {
                    if (vm.guardar()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onVolver()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GUARDAR EVENTO")
            }
        }
    }
}

@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    valido: Boolean = false,
    unaLinea: Boolean = true
) {
    // Colores del campo según el diseño de Figma: fondo verde azulado semitransparente y texto claro
    val contenedor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val textoCampo = MaterialTheme.colorScheme.onPrimary

    // Animación: el borde cambia suavemente de gris a rojo cuando hay error
    val colorBorde by animateColorAsState(
        targetValue = if (error != null) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "colorBorde"
    )

    Column {
        Text(etiqueta, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = unaLinea,
            minLines = if (unaLinea) 1 else 3,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Filled.Warning, contentDescription = "Error", tint = Color(0xFFFFB300))
                } else if (valido) {
                    Icon(Icons.Filled.Check, contentDescription = "Válido", tint = Color(0xFF2E7D32))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = contenedor,
                unfocusedContainerColor = contenedor,
                errorContainerColor = contenedor,
                focusedTextColor = textoCampo,
                unfocusedTextColor = textoCampo,
                errorTextColor = textoCampo,
                cursorColor = textoCampo,
                errorCursorColor = textoCampo,
                focusedBorderColor = colorBorde,
                unfocusedBorderColor = colorBorde,
                errorBorderColor = colorBorde
            )
        )
        AnimatedVisibility(visible = error != null) {
            Text(error ?: "", color = Color.Red)
        }
    }
}