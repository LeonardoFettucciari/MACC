// File: app/src/main/java/com/example/macc2025/navigation/AppNavHost.kt
package com.example.macc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.macc.presentation.ui.CameraScreen
import com.example.macc.presentation.ui.ProfileScreen
import com.example.macc.presentation.ui.UsernameScreen
import com.example.macc.presentation.ui.RankingScreen
import com.example.macc.presentation.ui.MapScreen
import com.example.macc.presentation.ui.SearchScreen
import com.example.macc.presentation.viewmodel.SearchViewModel
import com.example.macc.presentation.viewmodel.CameraViewModel
import com.example.macc.presentation.viewmodel.MapViewModel
import com.example.macc.presentation.viewmodel.ProfileViewModel

@Composable
fun AppNavHost() {
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
                val viewModel: SearchViewModel = hiltViewModel(parentEntry)

                SearchScreen(
                    navController = navController,
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
                val viewModel: MapViewModel = viewModel()

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
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)
                val cameraViewModel: CameraViewModel = hiltViewModel()

                CameraScreen(
                    navController = navController,
                    searchViewModel = searchViewModel,
                    viewModel = cameraViewModel
                )
            }

            // PROFILE SCREEN
            composable("profile") {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            // USERNAME SCREEN
            composable("username") {
                val viewModel: ProfileViewModel = hiltViewModel()
                UsernameScreen(navController = navController, viewModel = viewModel)
            }

            // RANKING SCREEN
            composable("ranking") {
                val viewModel: ProfileViewModel = hiltViewModel()
                RankingScreen(navController = navController, viewModel = viewModel)
            }
        }
    }
}
