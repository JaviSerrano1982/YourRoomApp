package com.example.yourroom.ui.theme.screens.succes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.yourroom.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// ---------------------------------------------------------------------
// PANTALLA DE ÉXITO (POST-REGISTRO y PUBLICACIÓN DE SALA)
// ---------------------------------------------------------------------

/**
 * Muestra una animación de éxito y un botón para volver al Login.
 *
 * Flujo:
 * - Carga y reproduce la animación Lottie (R.raw.success).
 * - Muestra título "¡Registro exitoso!".
 * -
 */
@Composable
fun SuccessScreen(
    title: String,              // Texto principal que se mostrará en la pantalla (ej. "¡Registro exitoso!" o "¡Sala publicada!")
    primaryText: String,        // Texto del botón principal (ej. "Iniciar sesión" o "Ir al inicio")
    onPrimaryClick: () -> Unit  // Acción a ejecutar al pulsar el botón (se recibe desde el NavHost)
) {
    // Cargamos la animación Lottie desde los recursos (R.raw.success)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))

    // Animamos la composición para que se ejecute automáticamente
    val progress by animateLottieCompositionAsState(composition)

    // Contenedor principal: ocupa toda la pantalla, con fondo blanco y padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Colocamos los elementos en columna y centrados horizontalmente
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Animación Lottie (icono de éxito)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Texto principal (título parametrizable)
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A1D37),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Botón principal con estilo y acción parametrizable
            Button(
                onClick = onPrimaryClick,
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A1D37),
                    contentColor = Color.White
                )
            ) {
                // Texto del botón (parametrizable)
                Text(primaryText)
            }
        }
    }
}


// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------

/**
 * Preview para verificar la pantalla de éxito en Android Studio.
 */
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SuccessScreenPreview() {
    SuccessScreen(
        onPrimaryClick = {},
        primaryText = "",
        title = "")
}
