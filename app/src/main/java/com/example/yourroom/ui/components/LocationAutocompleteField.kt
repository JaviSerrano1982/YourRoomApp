package com.example.yourroom.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.yourroom.location.MunicipiosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------
// COMPONENT: LocationAutocompleteField
// ------------------------------

/**
 * Campo de texto con autocompletado para introducir ubicaciones
 * (municipios en este caso). Incluye:
 *
 * - Entrada de texto con `TextField`.
 * - Lista desplegable de sugerencias filtradas en tiempo real.
 * - Ejemplo en el label si el campo está vacío y sin foco.
 * - Botón para borrar el contenido.
 * - Soporte para mostrar mensajes de error.
 *
 * @param value              Texto actual del campo.
 * @param label              Etiqueta del campo.
 * @param onValueChange      Callback cuando cambia el texto.
 * @param onSuggestionPicked Callback al seleccionar una sugerencia.
 * @param isSaving           Indica si se está guardando (bloquea campo).
 * @param isError            Indica si hay error en el campo.
 * @param errorMessage       Mensaje de error a mostrar.
 * @param colors             Colores personalizados del `TextField`.
 * @param enabled            Controla si está habilitado.
 * @param modifier           Modifier externo para personalización.
 * @param example            Texto de ejemplo mostrado bajo la etiqueta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAutocompleteField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    onSuggestionPicked: (String) -> Unit,
    isSaving: Boolean,
    isError: Boolean,
    errorMessage: String?,
    colors: TextFieldColors,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    example: String? = null
) {
    val ctx = LocalContext.current
    val all = remember { MunicipiosRepository.getUiList(ctx) } // Lista completa de municipios
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(emptyList<MunicipiosRepository.MunicipioUi>()) }

    /**
     * Recalcula las sugerencias según el texto introducido.
     * Solo busca cuando hay 2 o más caracteres.
     */
    fun recompute(query: String) {
        val q = query.trim()
        suggestions = if (q.length >= 2) MunicipiosRepository.filter(all, q) else emptyList()
        expanded = hasFocus && suggestions.isNotEmpty()
    }

    // Cierra el menú al guardar
    LaunchedEffect(isSaving) { if (isSaving) expanded = false }
    // Recalcula sugerencias cuando carga la lista de municipios
    LaunchedEffect(all) { recompute(value) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { wantOpen ->
                expanded = wantOpen && hasFocus && suggestions.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = value,
                onValueChange = { newVal ->
                    val clean = newVal.replace(Regex("\\s+"), " ")
                    onValueChange(clean)
                    // retrasa la búsqueda para no ejecutar en cada tecla
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(120)
                        recompute(clean)
                    }
                },
                label = {
                    // Si no hay foco ni texto → muestra ejemplo debajo del label
                    if (!hasFocus && value.isEmpty() && !example.isNullOrBlank()) {
                        Column {
                            Text(
                                label,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                example!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(label)
                    }
                },
                singleLine = true,
                isError = isError,
                enabled = enabled && !isSaving,   // respeta "enabled"
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { f ->
                        hasFocus = f.isFocused
                        expanded = f.isFocused && suggestions.isNotEmpty()
                    },
                colors = colors,
                trailingIcon = {
                    // Icono de borrar cuando hay texto
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onValueChange("")
                                recompute("")
                                focusRequester.requestFocus()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Borrar")
                        }
                    }
                }
            )

            // Menú desplegable de sugerencias
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                suggestions.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.label) },
                        onClick = {
                            onSuggestionPicked(s.label)
                            expanded = false
                            focusRequester.requestFocus()
                        }
                    )
                }
            }
        }

        // Mensaje de error bajo el campo
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}
