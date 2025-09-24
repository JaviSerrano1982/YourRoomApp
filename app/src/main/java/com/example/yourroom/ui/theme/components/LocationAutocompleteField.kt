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
    val all = remember { MunicipiosRepository.getUiList(ctx) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(emptyList<MunicipiosRepository.MunicipioUi>()) }

    fun recompute(query: String) {
        val q = query.trim()
        suggestions = if (q.length >= 2) MunicipiosRepository.filter(all, q) else emptyList()
        expanded = hasFocus && suggestions.isNotEmpty()
    }

    LaunchedEffect(isSaving) { if (isSaving) expanded = false }
    LaunchedEffect(all) { recompute(value) }

    Column(
        modifier = modifier.fillMaxWidth() // ✅ usa el modifier que recibes
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
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(120)
                        recompute(clean)
                    }
                },
                label = {
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
                enabled = enabled && !isSaving,   // ✅ respeta "enabled"
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { f ->
                        hasFocus = f.isFocused
                        expanded = f.isFocused && suggestions.isNotEmpty()
                    },
                colors = colors,                   // ✅ aplica los colores que pasas
                trailingIcon = {
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
