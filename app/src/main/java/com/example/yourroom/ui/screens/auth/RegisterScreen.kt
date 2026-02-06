package com.example.yourroom.ui.screens.auth

import android.util.Patterns
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.model.User
import com.example.yourroom.network.RetrofitClient
import com.example.yourroom.ui.components.YourRoomGradient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.yourroom.ui.components.AuthTextFieldColors

// ---------------------------------------------------------------------
// PANTALLA DE REGISTRO
// ---------------------------------------------------------------------

/**
 * Mantiene el estado local de los campos y ejecuta la lógica de validación
 * y registro de usuario contra el backend.
 *
 * Flujo:
 * 1. Valida que los campos no estén vacíos y que las contraseñas coincidan.
 * 2. Llama al backend vía Retrofit para registrar el usuario.
 * 3. Si es exitoso, navega a la pantalla "success".
 * 4. Si falla, muestra un mensaje de error en pantalla.
 */
@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var hasTriedSubmit by remember { mutableStateOf(false) }


    fun validateEmail(value: String): String? {
        if (value.isBlank()) return null // no molestamos mientras está vacío
        return if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) null
        else "El formato del email es incorrecto."
    }


    RegisterScreenContent(
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = {
            email = it
            // No validamos mientras escribe. Solo limpiamos el error si estaba visible.
            if (emailError != null) emailError = null
        },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        emailError = emailError,
        hasTriedSubmit = hasTriedSubmit,

        onRegisterClick = {
            // -----------------------------
            // Validación en cliente
            // -----------------------------
            hasTriedSubmit = true

            // Recalculamos error de email SOLO al pulsar registrar
            val currentEmailError = validateEmail(email)
            emailError = currentEmailError

            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorText = "Por favor, completa todos los campos."
                return@RegisterScreenContent
            }

            if (currentEmailError != null) {
                errorText = null // el error visible es el de debajo del email
                return@RegisterScreenContent
            }

            if (password != confirmPassword) {
                errorText = "Las contraseñas no coinciden."
                return@RegisterScreenContent
            }

            errorText = null

            // -----------------------------
            // Llamada al backend
            // -----------------------------
            scope.launch(Dispatchers.IO) {
                try {
                    val newUser = User(
                        name = name,
                        email = email,
                        password = password,
                        role = "USUARIO"
                    )

                    val response = RetrofitClient.api.register(newUser)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            navController.navigate("success_register") {
                                popUpTo("register") { inclusive = true }
                            }
                        } else {
                            // Si tu backend devuelve 409/400 puedes afinar esto luego
                            errorText = "El correo ya está registrado."
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorText = "Error de red. Inténtalo de nuevo."
                    }
                }
            }
        },

        onBackToLoginClick = {
            // Vuelve al login
            navController.popBackStack()
        },

        errorText = errorText
    )
}

// ---------------------------------------------------------------------
// UI DE REGISTRO
// ---------------------------------------------------------------------

/**
 * Dibuja la UI del registro:
 * - Fondo con degradado y logo.
 * - Campos: nombre, email, contraseña, confirmar contraseña.
 * - Toggle de visibilidad de contraseña (compartido por ambos campos).
 * - Botones "Registrarse" y "Iniciar sesión".
 * - Mensaje de error si existe.
 */
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
    errorText: String?,
    emailError: String?,
    hasTriedSubmit: Boolean,

) {
    var passwordVisible by remember { mutableStateOf(false) }
    val showEmailError = hasTriedSubmit && emailError != null
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
            // Logo
            Image(
                painter = painterResource(id = R.drawable.your_room_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Nombre
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors(),
                isError = showEmailError,
                supportingText = {
                    if (showEmailError) {
                        Text(
                            text = emailError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                colors = AuthTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                colors = AuthTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fila de botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                // Botón Registrarse
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

                // Botón Iniciar sesión
                OutlinedButton(
                    onClick = onBackToLoginClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0A1D37)
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

            // Mensaje de error
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

// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------

/**
 * Preview para visualizar la pantalla de registro en Android Studio.
// */
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
        errorText = null,
        emailError=null,
        hasTriedSubmit = false
    )
}
