package com.example.yourroom.navigation

import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.yourroom.ui.theme.screens.FavoritesScreen
import com.example.yourroom.ui.theme.screens.HomeScreen
import com.example.yourroom.ui.theme.screens.ProfileScreen
import com.example.yourroom.ui.theme.screens.PublishScreen
import com.example.yourroom.ui.theme.screens.SearchScreen
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Brush


sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Search : BottomNavItem("search", Icons.Default.Search, "Buscar")
    object Publish : BottomNavItem("publish", Icons.Default.Add, "Publicar")
    object Favorites : BottomNavItem("favorites", Icons.Default.Star, "Favoritos")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Cuenta")
}


@Composable
fun MainNavGraph(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Publish,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A1D37),
                modifier = Modifier
                    .height(75.dp)
                    .navigationBarsPadding()

            ) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    if (item == BottomNavItem.Publish) {
                        // Botón central personalizado
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
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color(0xFF6750A4)  // un violeta más claro o azulado
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)

                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(28.dp),
                                        tint = if (isSelected) Color(0xFF2FE2EC) else Color.White
                                    )

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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Search.route) { SearchScreen() }
            composable(BottomNavItem.Publish.route) { PublishScreen() }
            composable(BottomNavItem.Favorites.route) { FavoritesScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainNavGraphPreview() {
    val navController = rememberNavController()
    MainNavGraph(navController = navController)
}
