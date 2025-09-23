package com.example.yourroom.ui.theme.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------
// GRADIENTE PRINCIPAL DE YOUR ROOM
// ---------------------------------------------------------------------

/**
 * Degradado vertical usado como fondo general de la app.
 */
val YourRoomGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFFFFF), // 0%   → Blanco
        0.3f to Color(0xFFFFFFFF), // 20%  → Blanco (mantiene claridad en la parte alta)
        1f to Color(0xFF2FE2EC), // 60%  → Celeste YourRoom
        //1.0f to Color(0xFF0A1D37)  // 100% → Azul oscuro de marca
    )
)

// ---------------------------------------------------------------------
// GRADIENTE DE ÉXITO (PANTALLA POST-REGISTRO)
// ---------------------------------------------------------------------

/**
 * Degradado vertical para pantallas de éxito / confirmación.
 */
val SuccesGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF2FE2EC), // 0%   → Azul en la parte superior
        0.2f to Color(0xFFFFFFFF), // 20%  → Blanco subiendo hacia el centro
        0.8f to Color(0xFFFFFFFF), // 80%  → Blanco bajando desde el centro
        1.0f to Color(0xFF2FE2EC)  // 100% → Azul en la parte inferior
    )
)
