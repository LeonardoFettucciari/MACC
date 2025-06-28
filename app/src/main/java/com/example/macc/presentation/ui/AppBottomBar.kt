package com.example.macc.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("search", "Search", Icons.Default.Place),
        BottomNavItem("ranking", "Ranking", Icons.Default.EmojiEvents),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("search") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
