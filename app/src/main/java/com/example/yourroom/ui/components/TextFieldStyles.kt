package com.example.yourroom.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Devuelve un conjunto de colores personalizados para un `TextField`
 * con fondo transparente.
 *
 * - Colores del contenedor (focused, unfocused, disabled) → Transparent.
 * - Indicador de foco → color primario del tema.
 * - Indicador sin foco → color onSurface con opacidad reducida.
 * - Color del cursor → color primario del tema.
 *
 * Útil para integrar campos de texto en fondos personalizados
 * sin mostrar el "cajetín" típico del TextField.
 */
@Composable
fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    cursorColor = MaterialTheme.colorScheme.primary
)
