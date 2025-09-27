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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    val canPublish by vm.canPublish.collectAsStateWithLifecycle()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Publish,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF0A1D37),
        modifier = Modifier
            .height(65.dp)
            .navigationBarsPadding()
            .padding(top = 6.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            if (item == BottomNavItem.Publish) {
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (canPublish) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Debes completar tu perfil antes de publicar una sala",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    icon = {
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
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            // Dibuja según el tipo disponible
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
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF381E72),
                        unselectedIconColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
