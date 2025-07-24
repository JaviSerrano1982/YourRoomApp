package com.example.yourroom.ui.theme.screens

import android.R.color.white
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
import com.example.yourroom.ui.theme.SuccesGradient
import com.example.yourroom.ui.theme.YourRoomGradient

@Composable
fun SuccessScreen(onNavigateToLogin: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    val progress by animateLottieCompositionAsState(composition)



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SuccesGradient)
            .padding(32.dp),

        contentAlignment = Alignment.Center,

    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Registro exitoso!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A1D37),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToLogin,
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A1D37),
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar sesión")
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SuccessScreenPreview() {
    SuccessScreen(onNavigateToLogin = {})
}
