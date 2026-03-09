package com.example.yourroom.ui.utils

import com.example.yourroom.model.SpaceResponse
import java.math.RoundingMode

fun formatPrice(space: SpaceResponse): String {
    val price = space.hourlyPrice ?: return "Precio no disponible"

    val normalized = price.stripTrailingZeros()

    return if (normalized.scale() <= 0) {
        "${normalized.toInt()} € / hora"
    } else {
        "${price.setScale(2, RoundingMode.HALF_UP)} € / hora"
    }
}