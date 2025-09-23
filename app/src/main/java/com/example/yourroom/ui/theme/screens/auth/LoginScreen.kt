package com.example.yourroom.ui.theme.screens.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.model.AuthRequest
import com.example.yourroom.network.RetrofitClient
import com.example.yourroom.ui.theme.components.YourRoomGradient
import com.example.yourroom.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------------------------------------------------------------------
// PANTALLA DE LOGIN
// ---------------------------------------------------------------------

/**
 * Mantiene el estado de email/contraseña y ejecuta el flujo de autenticación:
 * 1) Valida que los campos no estén vacíos.
 * 2) Llama al backend con Retrofit (email/password) → recibe JWT + userId.
 * 3) Guarda token y userId en DataStore (sesión local).
 * 4) Solicita al backend un Custom Token de Firebase y hace sign-in en Firebase.
 * 5) Navega a "home" limpiando el back stack de "login".
 *
 * Nota: se inyecta [UserProfileViewModel] vía Hilt aunque no se use aquí.
 * Mantenerlo es intencional para no alterar el grafo de DI ni el comportamiento.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun LoginScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel = hiltViewModel()
) {
    // Estado de formulario y error general
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
            // -----------------------------
            // Validación mínima en cliente
            // -----------------------------
            if (email.isNotBlank() && password.isNotBlank()) {
                scope.launch {
                    try {
                        // -----------------------------
                        // 1) Autenticación en backend
                        // -----------------------------
                        val response = RetrofitClient.api.login(AuthRequest(email, password))

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                val token = body.token
                                val userId = body.userId

                                // -----------------------------
                                // 2) Persistencia local (DataStore)
                                // -----------------------------
                                val userPrefs = UserPreferences(context)
                                userPrefs.setUserLoggedIn(true)
                                userPrefs.saveAuthToken(token)
                                userPrefs.saveUserId(userId)

                                try {
                                    // -----------------------------
                                    // 3) Obtener Custom Token Firebase
                                    // -----------------------------
                                    val bearer = "Bearer $token"
                                    val firebaseToken =
                                        RetrofitClient.api.getFirebaseToken(bearer).token

                                    // -----------------------------
                                    // 4) Sign-in en Firebase
                                    // -----------------------------
                                    try {
                                        FirebaseAuth.getInstance().signOut()
                                        FirebaseAuth.getInstance()
                                            .signInWithCustomToken(firebaseToken)
                                            .await()

                                        Log.d(
                                            "FirebaseAuth",
                                            "UID en Firebase: ${FirebaseAuth.getInstance().currentUser?.uid}"
                                        )
                                    } catch (e: Exception) {
                                        Log.e(
                                            "FirebaseAuth",
                                            "Error al loguear en Firebase: ${e.message}"
                                        )
                                        errorText =
                                            "Error al conectar con Firebase: ${e.message}"
                                        return@launch
                                    }

                                    // -----------------------------
                                    // 5) Navegación a Home
                                    // -----------------------------
                                    errorText = null
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorText = "Error al conectar con Firebase"
                                    return@launch
                                }
                            } else {
                                errorText = "Respuesta vacía del servidor."
                            }
                        } else {
                            errorText = "Email o contraseña incorrectos."
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
            // Navega a la pantalla de registro
            navController.navigate("register")
        },

        errorText = errorText
    )
}

// ---------------------------------------------------------------------
// UI DE LOGIN
// ---------------------------------------------------------------------

/**
 * Dibuja la UI de la pantalla de login:
 * - Fondo con degradado [YourRoomGradient] y logo.
 * - Campos de Email y Contraseña con toggle de visibilidad.
 * - Fila de botones: "Entrar" (filled) y "Registrarse" (outlined).
 * - Mensaje de error centrado si existe.
 */
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
    // Solo afecta a la UI (mostrar/ocultar contraseña)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.your_room_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña (con icono para alternar visibilidad)
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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
                    .height(48.dp)
            ) {
                // Botón Entrar
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

                // Botón Registrarse
                OutlinedButton(
                    onClick = onRegisterClick,
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
                    Text("Registrarse")
                }
            }

            // Mensaje de error (si aplica)
            errorText?.let {
                Text(
                    text = it,
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

// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------

/**
 * Preview para visualizar la pantalla de login en Android Studio.
 */
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
