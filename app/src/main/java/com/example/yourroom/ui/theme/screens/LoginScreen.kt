package com.example.yourroom.ui.theme.screens


import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.yourroom.model.AuthRequest
import com.example.yourroom.network.RetrofitClient

import kotlinx.coroutines.launch
import com.example.yourroom.ui.theme.YourRoomGradient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import com.example.yourroom.navigation.BottomNavItem
import kotlinx.coroutines.flow.first


@Composable
fun LoginScreen(navController: NavHostController) {

    Modifier.background(YourRoomGradient)


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                    try {
                        val response = RetrofitClient.api.login(AuthRequest(email, password))

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                val token = body.token
                                val userId = body.userId

                                val userPrefs = UserPreferences(context)
                                userPrefs.setUserLoggedIn(true)
                                userPrefs.saveAuthToken(token)
                                userPrefs.saveUserId(userId)

                                errorText = null

                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {

                                errorText = "Respuesta vacía del servidor."

                            }
                        } else {
                            errorText = "Email o contraseña incorrectos. "
                        }
                    } catch (e: Exception) {
                       e.printStackTrace()
                        errorText = "Error al conectar con el servidor."
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
    errorText: String?,

) {
    var passwordVisible by remember { mutableStateOf(false) }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YourRoomGradient),
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
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fila con dos botones: Entrar y Registrarse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)  // Ajusta si quieres otro alto
            ) {
                // Botón Entrar activo
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0A1D37)
                    ),
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        bottomStart = 24.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Entrar")
                }



                //Botón registrarse
                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Registrarse")
                }
            }
            if (errorText != null ) {
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }




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
        errorText = null,

    )
}
