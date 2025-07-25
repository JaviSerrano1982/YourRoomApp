package com.example.yourroom.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.yourroom.ui.theme.screens.FavoritesScreen
import com.example.yourroom.ui.theme.screens.HomeScreen
import com.example.yourroom.ui.theme.screens.ProfileScreen
import com.example.yourroom.ui.theme.screens.PublishScreen
import com.example.yourroom.ui.theme.screens.SearchScreen

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Search : BottomNavItem("search", Icons.Default.Search, "Buscar")
    object Publish : BottomNavItem("publish", Icons.Default.Add, "Publicar")
    object Favorites : BottomNavItem("favorites", Icons.Default.Star, "Favoritos")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Cuenta")
}

@Composable
fun MainNavGraph(navController: NavHostController) {
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(BottomNavItem.Publish.route)
            }) {
                Icon(imageVector = BottomNavItem.Publish.icon, contentDescription = "Publicar")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val leftItems = listOf(BottomNavItem.Home, BottomNavItem.Search)
                    val rightItems = listOf(BottomNavItem.Favorites, BottomNavItem.Profile)

                    Row {
                        leftItems.forEach { item ->
                            NavBarItem(navController, item)
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp)) // espacio reservado al FAB

                    Row {
                        rightItems.forEach { item ->
                            NavBarItem(navController, item)
                        }
                    }
                }
            }
        }


    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Search.route) { SearchScreen() }
            composable(BottomNavItem.Publish.route) { PublishScreen() }
            composable(BottomNavItem.Favorites.route) { FavoritesScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}
@Composable
fun NavBarItem(navController: NavHostController, item: BottomNavItem) {
    IconButton(onClick = {
        navController.navigate(item.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(item.icon, contentDescription = item.label)
            Text(item.label, style = MaterialTheme.typography.labelSmall)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainNavGraphPreview() {
    val navController = rememberNavController()
    MainNavGraph(navController = navController)
}
