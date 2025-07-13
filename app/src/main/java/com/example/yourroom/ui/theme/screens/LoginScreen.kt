package com.example.yourroom.ui.theme.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.datastore.UserPreferences

import kotlinx.coroutines.launch



@Composable
fun LoginScreen(navController: NavHostController) {
  
    val gradient = Brush.verticalGradient(
        listOf( Color(0xFFFFFFFF)
                ,Color(0xFF2FE2EC),Color(0xFF0A1D37))
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LoginScreenContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLoginClick = {
            if (email.isNotBlank() && password.isNotBlank()) {
                scope.launch {
                    UserPreferences(context).setUserLoggedIn(true)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } else {
                errorText = "Por favor, completa los campos."
            }
        },
        onRegisterClick = {
            navController.navigate("register")
        },
        errorText = errorText
    )

}

@Composable
fun LoginScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    errorText: String?
) {
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFFFFFFF),  // blanco desde el 0%
            0.2f to Color(0xFFFFFFFF),  // blanco hasta el 50%
            0.6f to Color(0xFF2FE2EC),  // cian entre 50%-70%
            1.0f to Color(0xFF0A1D37)   // azul oscuro del 70% al 100%
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .offset(y = (-60).dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Image(
                painter = painterResource(id = R.drawable.your_room_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(260.dp)

            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,              // fondo del botón
                    contentColor = Color(0xFF0A1D37)           // color del texto
                )
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            errorText?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onRegisterClick)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenContent(
        email = "demo@yourroom.com",
        onEmailChange = {},
        password = "123456",
        onPasswordChange = {},
        onLoginClick = {},
        onRegisterClick = {},
        errorText = null
    )
}

@Composable
fun LoginRegisterToggleScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    errorText: String? = null
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegisterMode) "Registrarse" else "Iniciar sesión",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Login", color = Color.White)
            Switch(
                checked = isRegisterMode,
                onCheckedChange = { isRegisterMode = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White
                )
            )
            Text("Registro", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { if (isRegisterMode) onRegisterClick() else onLoginClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF0A1D37)
            )
        ) {
            Text(if (isRegisterMode) "Registrarse" else "Entrar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorText?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
