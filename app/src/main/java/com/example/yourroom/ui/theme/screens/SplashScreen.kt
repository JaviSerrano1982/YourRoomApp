package com.example.yourroom.ui.theme.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.datastore.UserPreferences
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------
// PANTALLA DE SPLASH
// ---------------------------------------------------------------------

/**
 * Pantalla de bienvenida (Splash).
 *
 * Lógica:
 * - Muestra el logo, tagline y un indicador de progreso con fondo animado.
 * - Tras 2.5 segundos comprueba si el usuario ya está logueado.
 * - Si está logueado → navega a "home".
 * - Si no está logueado → navega a "login".
 */
@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Corrutina que espera 2.5s y decide la navegación
    LaunchedEffect(Unit) {
        delay(2500)
        val isLoggedIn = UserPreferences(context).isUserLoggedIn()
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // Dibuja la UI del splash
    SplashScreenContent()
}

// ---------------------------------------------------------------------
// UI DEL SPLASH
// ---------------------------------------------------------------------

/**
 * Dibuja el contenido del splash:
 * - Fondo degradado radial + capa azul superior.
 * - Logo en el centro.
 * - Texto "Alquila. Usa. Libera." debajo del logo.
 * - Indicador de carga circular.
 *
 * Incluye animaciones de aparición (fade in) para el logo y el cargador.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SplashScreenContent() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Offset vertical para ajustar el centro del gradiente
        val offsetY = -maxHeight.value * 0.1f

        // Gradiente radial de fondo
        val radialGradient = Brush.radialGradient(
            colorStops = arrayOf(
                0.55f to Color.White,
                1.0f to Color(0xFF90ECF3)
            ),
            center = Offset(
                x = maxWidth.value / 0.73f,
                y = maxHeight.value / 0.72f + offsetY
            ),
            radius = Float.POSITIVE_INFINITY
        )

        // Caja principal con fondo degradado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = radialGradient)
        ) {
            // Capa superior con degradado vertical azul oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFBDF8FB).copy(alpha = 0f), // arriba
                                Color.Transparent,                  // centro
                                Color(0xFFBDF8FB).copy(alpha = 0f)  // abajo
                            )
                        )
                    )
            )

            // Animación de fade in del logo
            val logoAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "LogoFade"
            )

            // Animación de fade in del cargador (con retraso)
            val progressAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    delayMillis = 1000,
                    easing = FastOutSlowInEasing
                ),
                label = "ProgressFade"
            )

            // Contenido centrado: logo, tagline y cargador
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.your_room_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(260.dp)
                        .offset(y = 30.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Texto slogan
                Text(
                    text = "Alquila. Usa. Libera.",
                    color = Color(0xFF0A1D37),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(y = 20.dp)
                )

                // Indicador de progreso
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .offset(y = 80.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------

/**
 * Preview de la pantalla de Splash en Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreenContent()
}
