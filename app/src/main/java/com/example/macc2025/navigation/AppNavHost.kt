// File: app/src/main/java/com/example/macc2025/navigation/AppNavHost.kt
package com.example.macc2025.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.macc2025.camera.CameraScreen
import com.example.macc2025.maps.MapScreen
import com.example.macc2025.maps.SearchScreen
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun AppNavHost(
    placesClient: PlacesClient
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "mainGraph"
    ) {
        navigation(
            startDestination = "search",
            route = "mainGraph"
        ) {
            // SEARCH SCREEN
            composable("search") { backStackEntry ->
                // safe to call getBackStackEntry here
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mainGraph")
                }
                val viewModel: MainViewModel = viewModel(parentEntry)

                SearchScreen(
                    navController = navController,
                    placesClient = placesClient,
                    viewModel = viewModel
                )
            }

            // MAP SCREEN
            composable(
                route = "map/{lat}/{lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType },
                    navArgument("lng") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mainGraph")
                }
                val viewModel: MainViewModel = viewModel(parentEntry)

                val lat = backStackEntry.arguments
                    ?.getFloat("lat")
                    ?.toDouble()
                    ?: 0.0
                val lng = backStackEntry.arguments
                    ?.getFloat("lng")
                    ?.toDouble()
                    ?: 0.0

                MapScreen(
                    navController = navController,
                    lat = lat,
                    lng = lng,
                    viewModel = viewModel
                )
            }

            // CAMERA SCREEN
            composable("camera") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mainGraph")
                }
                val viewModel: MainViewModel = viewModel(parentEntry)

                CameraScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
