package com.example.yourroom.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val YourRoomGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFFFFF),
        0.2f to Color(0xFFFFFFFF),
        0.6f to Color(0xFF2FE2EC),
        1.0f to Color(0xFF0A1D37)
    )
)

val SuccesGradient = Brush.verticalGradient(
    colorStops = arrayOf(

        0.0f to Color(0xFF2FE2EC),   // Azul en la parte superior
        0.2f to Color(0xFFFFFFFF),   // Blanco subiendo hacia el centro
        0.8f to Color(0xFFFFFFFF),   // Blanco bajando desde el centro
        1.0f to Color(0xFF2FE2EC)    // Azul en la parte inferior

    )
)