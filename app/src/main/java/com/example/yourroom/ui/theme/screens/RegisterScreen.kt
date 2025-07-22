package com.example.yourroom.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.model.User
import com.example.yourroom.network.RetrofitClient
import com.example.yourroom.ui.theme.YourRoomGradient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    RegisterScreenContent(
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        onRegisterClick = {
            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorText = "Por favor, completa todos los campos."
            } else if (password != confirmPassword) {
                errorText = "Las contraseñas no coinciden."
            } else {
                errorText = null

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val newUser = User(
                            name = name,
                            email = email,
                            password = password,
                            role = "TRAINER" // o "USUARIO" por defecto
                        )

                        val response = RetrofitClient.api.register(newUser)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                // Registro correcto: ir al login
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                errorText = "El correo ya está registrado"
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorText = "Error de red: ${e.message}"
                        }
                    }
                }
            }
        }
        ,
        onBackToLoginClick = {
            navController.popBackStack()
        },
        errorText = errorText
    )
}

@Composable
fun RegisterScreenContent(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    errorText: String?
) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.your_room_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                // Botón Registrarse (activo)
                Button(
                    onClick = onRegisterClick,
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
                    Text("Registrarse")
                }

                // Botón Iniciar sesión (solo navegación)
                OutlinedButton(
                    onClick = onBackToLoginClick,
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
                    Text("Iniciar sesión")
                }
            }


            errorText?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreenContent(
        name = "Javier",
        onNameChange = {},
        email = "javier@email.com",
        onEmailChange = {},
        password = "123456",
        onPasswordChange = {},
        confirmPassword = "123456",
        onConfirmPasswordChange = {},
        onRegisterClick = {},
        onBackToLoginClick = {},
        errorText = null
    )
}



