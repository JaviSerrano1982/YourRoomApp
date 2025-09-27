package com.example.yourroom.navigation

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.yourroom.R
import com.example.yourroom.viewmodel.UserProfileViewModel

// ---------------------------------------------------------------------
// DEFINICIÓN DE ITEMS DE NAVEGACIÓN INFERIOR
// ---------------------------------------------------------------------

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val imageVector: ImageVector? = null,  // para Material Icons
    @DrawableRes val iconResId: Int? = null // para SVG/VectorDrawable importado
) {
    object Home : BottomNavItem("home", "Inicio", imageVector = Icons.Default.Home)
    object Search : BottomNavItem("search", "Mis salas", iconResId = R.drawable.door_open)
    object Publish : BottomNavItem("publish", "Publicar", imageVector = Icons.Default.Add)
    object Favorites : BottomNavItem("favorites", "Favoritos", imageVector = Icons.Default.Favorite)
    object Profile : BottomNavItem("profile", "Mi Perfil", imageVector = Icons.Default.Person)
}

// ---------------------------------------------------------------------
// COMPONENTE: BARRA DE NAVEGACIÓN INFERIOR
// ---------------------------------------------------------------------

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    vm: UserProfileViewModel
) {
    val canPublish by vm.canPublish.collectAsStateWithLifecycle()

    var showWarning by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Publish,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Aviso modal cuando no puede publicar
    if (showWarning) {
        PublishWarningCard(
            message = "Debes completar tu perfil antes de publicar una sala",
            onDismiss = { showWarning = false }
        )
    }

    NavigationBar(
        containerColor = Color(0xFF0A1D37),
        modifier = Modifier
            .height(65.dp)
            .navigationBarsPadding()
            .padding(top = 6.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (item == BottomNavItem.Publish) {
                        if (canPublish) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            showWarning = true
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (item == BottomNavItem.Publish) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.White, Color(0xFF6750A4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.imageVector!!,
                                contentDescription = "Publicar",
                                tint = Color(0xFF381E72)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            if (item.iconResId != null) {
                                Icon(
                                    painter = painterResource(item.iconResId),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(30.dp),
                                    tint = if (isSelected) Color(0xFF2FE2EC) else Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = item.imageVector!!,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isSelected) Color(0xFF2FE2EC) else Color.White
                                )
                            }
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFF2FE2EC) else Color.White
                            )
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// ---------------------------------------------------------------------
// Diálogo con Card para el aviso
// ---------------------------------------------------------------------

@Composable
private fun PublishWarningCard(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {  },
        properties = DialogProperties(dismissOnClickOutside = false)

    ) {
        // Caja para anclar la card abajo y dejar márgenes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1D37) ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                        ) {
                        Text("Entendido")
                    }
                }
            }
        }
    }
}