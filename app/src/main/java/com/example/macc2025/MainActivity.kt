package com.example.macc2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.macc2025.maps.MapScreen
import com.example.macc2025.maps.SearchScreen
import com.example.macc2025.ui.theme.MACC2025Theme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        val placesClient = Places.createClient(this)

        setContent {
            MACC2025Theme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "search"
                ) {
                    composable("search") {
                        SearchScreen(navController, placesClient)
                    }
                    composable(
                        route = "map/{lat}/{lng}",
                        arguments = listOf(
                            navArgument("lat") { type = NavType.FloatType },
                            navArgument("lng") { type = NavType.FloatType }
                        )
                    ) { backStackEntry ->
                        val lat = backStackEntry.arguments?.getFloat("lat") ?: 0f
                        val lng = backStackEntry.arguments?.getFloat("lng") ?: 0f
                        MapScreen(lat.toDouble(), lng.toDouble())
                    }
                }
            }
        }
    }
}